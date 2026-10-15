package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.toDownloadProgressOrNull
import app.vercors.core.domain.toFractionOrNull
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.usecase.ImportPrismInstanceFilesUseCase
import app.vercors.feature.instance.domain.usecase.RefreshInstalledContentUseCase
import app.vercors.feature.instance.domain.usecase.RefreshInstalledWorldsUseCase
import app.vercors.feature.instance.domain.usecase.UpdateInstanceStatusUseCase
import app.vercors.feature.project.domain.model.modpack.ModpackInstallError
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.pack.*
import app.vercors.feature.project.domain.repository.InstalledPackRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

// An update keeps the receiver's worlds, settings and icon
@Inject
class InstallLocalPackUseCase(
    private val installModpackFiles: InstallModpackFilesUseCase,
    private val importPrismInstanceFiles: ImportPrismInstanceFilesUseCase,
    private val extractVercorsPackIcon: ExtractVercorsPackIconUseCase,
    private val recordModpackFileOrigins: RecordModpackFileOriginsUseCase,
    private val installedPackRepository: InstalledPackRepository,
    private val updateInstanceStatus: UpdateInstanceStatusUseCase,
    private val refreshInstalledContent: RefreshInstalledContentUseCase,
    private val refreshInstalledWorlds: RefreshInstalledWorldsUseCase,
) {
    context(_: Raise<LocalPackError>)
    suspend operator fun invoke(pack: LocalPack, instanceId: InstanceId, isUpdate: Boolean): LocalPackInstallResult {
        val result = when (pack) {
            is LocalPack.Vercors -> {
                val installedPack = installManifest(pack, pack.info.manifest, instanceId, isUpdate)
                val iconPath = pack.info.iconExtension
                    ?.takeUnless { isUpdate }
                    ?.let { extractVercorsPackIcon(pack.sourcePath, it) }
                LocalPackInstallResult(installedPack, iconPath)
            }

            is LocalPack.Prism -> importPrism(pack, instanceId, isUpdate)
            is LocalPack.Mrpack -> LocalPackInstallResult(
                installManifest(pack, pack.manifest, instanceId, isUpdate),
                iconPath = null
            )
        }
        installedPackRepository.save(instanceId, result.installedPack)
        refreshContent(instanceId)
        return result
    }

    context(_: Raise<LocalPackError>)
    private suspend fun installManifest(
        pack: LocalPack,
        manifest: ModpackManifest,
        instanceId: InstanceId,
        isUpdate: Boolean
    ): InstalledPack {
        val result = recover({
            installModpackFiles(
                manifest,
                instanceId,
                localSourcePath = pack.sourcePath,
                preserveUserState = isUpdate
            ) { progress ->
                updateInstanceStatus(
                    instanceId,
                    InstanceStatus.Installing.DownloadingFiles(progress.toDownloadProgressOrNull())
                )
            }
        }) { error: ModpackInstallError ->
            logger.error { "Local pack install failed for $instanceId: $error" }
            raise(LocalPackError.InstallFailed)
        }
        recordModpackFileOrigins(instanceId, manifest.platformId, result.files)
        return manifest.toInstalledPack(pack.name, pack.modpackInfo, result.overridePaths)
    }

    context(_: Raise<LocalPackError>)
    private suspend fun importPrism(
        pack: LocalPack.Prism,
        instanceId: InstanceId,
        isUpdate: Boolean
    ): LocalPackInstallResult {
        val result = recover({
            importPrismInstanceFiles(pack.sourcePath, instanceId, pack.info, preserveUserState = isUpdate) { progress ->
                updateInstanceStatus(instanceId, InstanceStatus.Installing.ExtractingFiles(progress.toFractionOrNull()))
            }
        }) { error: LocalImportError ->
            logger.error { "Prism import failed for $instanceId: $error" }
            raise(LocalPackError.InstallFailed)
        }
        val installedPack = InstalledPack(
            name = pack.name,
            versionName = pack.versionName,
            modpackInfo = pack.modpackInfo,
            files = result.extractedPaths.map(::InstalledPackFile)
        ).withoutUserState()
        return LocalPackInstallResult(installedPack, iconPath = result.iconPath.takeUnless { isUpdate })
    }

    // Bulk write outside the scanners' APIs: refresh explicitly rather than relying on the watcher
    private suspend fun refreshContent(instanceId: InstanceId) {
        ContentKind.entries.forEach { refreshInstalledContent(it, instanceId) }
        refreshInstalledWorlds(instanceId)
    }
}
