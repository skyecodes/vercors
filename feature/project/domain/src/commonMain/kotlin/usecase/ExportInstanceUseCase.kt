package app.vercors.feature.project.domain.usecase

import app.vercors.core.domain.toFractionOrNull
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ModProvider
import app.vercors.feature.instance.domain.model.export.InstanceExportError
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.instance.domain.model.export.InstanceExportFormat
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import app.vercors.feature.instance.domain.repository.InstanceExportRepository
import app.vercors.feature.instance.domain.repository.InstanceRepository
import app.vercors.feature.project.domain.model.FileHashes
import app.vercors.feature.project.domain.model.ProjectDetailsError
import app.vercors.feature.project.domain.model.folderName
import app.vercors.feature.project.domain.model.pack.InstanceExportOptions
import app.vercors.feature.project.domain.model.pack.InstanceExportSummary
import app.vercors.feature.project.domain.model.pack.PackFileReference
import app.vercors.feature.project.domain.repository.InstancePackRepository
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.withError
import arrow.core.raise.recover
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull

private val logger = KotlinLogging.logger {}

private const val COLLECT_PROGRESS_END = 0.3

@Inject
class ExportInstanceUseCase(
    private val instanceRepository: InstanceRepository,
    private val instanceExportRepository: InstanceExportRepository,
    private val instanceContentRepository: InstanceContentRepository,
    private val instancePackRepository: InstancePackRepository,
    private val projectRepository: ProjectRepository,
) {
    context(_: Raise<InstanceExportError>)
    suspend operator fun invoke(
        instanceId: InstanceId,
        format: InstanceExportFormat,
        entries: Set<String>,
        options: InstanceExportOptions,
        targetPath: String,
        onProgress: (Progress) -> Unit = {}
    ): InstanceExportSummary {
        val instance = withError({ _: InstanceError -> InstanceExportError.ReadFailed }) {
            instanceRepository.findInstance(instanceId)
        }
        val resolveReferences = when (format) {
            InstanceExportFormat.Prism -> false
            InstanceExportFormat.Modrinth -> true
            InstanceExportFormat.Vercors -> options.referenceKnownFiles
        }
        val files = instanceExportRepository.collectFiles(
            instanceId,
            entries,
            hashContent = resolveReferences,
            onProgress = onProgress.inInterval(0.0, COLLECT_PROGRESS_END)
        )
        val references = when {
            !resolveReferences -> emptyList()
            format == InstanceExportFormat.Modrinth ->
                findHashMatchReferences(MODRINTH_PLATFORM_ID, files, requireMrpackHost = true)

            else -> findVercorsReferences(instanceId, files)
        }
        val referencedPaths = references.mapTo(HashSet()) { it.relativePath }
        val overrides = files.filter { it.relativePath !in referencedPaths }
        val writeProgress = onProgress.inInterval(COLLECT_PROGRESS_END, 1.0)
        when (format) {
            InstanceExportFormat.Prism ->
                instanceExportRepository.writePrismExport(instance, files, targetPath, writeProgress)

            InstanceExportFormat.Modrinth -> instancePackRepository.writeMrpack(
                instance,
                references,
                overrides,
                options.versionId,
                options.summary,
                targetPath,
                writeProgress
            )

            InstanceExportFormat.Vercors ->
                instancePackRepository.writeVercorsPack(instance, references, overrides, targetPath, writeProgress)
        }
        return InstanceExportSummary(referencedCount = references.size, bundledCount = overrides.size)
    }

    private suspend fun findVercorsReferences(
        instanceId: InstanceId,
        files: List<InstanceExportFile>
    ): List<PackFileReference> {
        val hashedFiles = files.filter { it.sha1 != null }.associateBy { it.relativePath }
        val recordedOrigins = ContentKind.entries.flatMap { kind ->
            val installed = instanceContentRepository.observeInstalledContent(kind, instanceId).now().valueOrNull
            installed.orEmpty().flatMap { content ->
                val file = hashedFiles["${kind.folderName}/${content.fileName}"] ?: return@flatMap emptyList()
                content.providers.map { file to it }
            }
        }
        val verified = recordedOrigins.groupBy { (_, provider) -> provider.platformId }
            .flatMap { (platformId, origins) -> verifyOrigins(platformId, origins) }
            .distinctBy { it.relativePath }
        val verifiedPaths = verified.mapTo(HashSet()) { it.relativePath }
        val unverified = hashedFiles.values.filter { it.relativePath !in verifiedPaths }
        // Modrinth first: its files never need a manual download
        val modrinthMatches = findHashMatchReferences(MODRINTH_PLATFORM_ID, unverified, requireMrpackHost = false)
        val modrinthPaths = modrinthMatches.mapTo(HashSet()) { it.relativePath }
        val unmatched = unverified.filter { it.relativePath !in modrinthPaths }
        val curseForgeMatches = findHashMatchReferences(CURSEFORGE_PLATFORM_ID, unmatched, requireMrpackHost = false)
        return verified + modrinthMatches + curseForgeMatches
    }

    private suspend fun verifyOrigins(
        platformId: String,
        origins: List<Pair<InstanceExportFile, ModProvider>>
    ): List<PackFileReference> {
        val remoteFiles = recover({
            projectRepository.getProjectFilesByIds(
                platformId,
                origins.map { (_, provider) -> provider.fileId }.distinct()
            )
        }) { error: ProjectDetailsError ->
            logger.warn { "Couldn't verify $platformId origins, bundling these files instead: $error" }
            emptyList()
        }.associateBy { it.id }
        return selectVerifiedReferences(platformId, origins, remoteFiles)
    }

    private suspend fun findHashMatchReferences(
        platformId: String,
        files: List<InstanceExportFile>,
        requireMrpackHost: Boolean
    ): List<PackFileReference> {
        val hashes = files.mapNotNull { file -> file.sha1?.let { FileHashes(it, file.curseForgeFingerprint) } }
            .distinctBy { it.sha1 }
        if (hashes.isEmpty()) return emptyList()
        val matches = recover({
            projectRepository.findFilesByHashes(platformId, hashes)
        }) { error: ProjectDetailsError ->
            logger.warn { "$platformId hash lookup failed, bundling these files instead: $error" }
            emptyList()
        }.associateBy { it.sha1 }
        return selectHashMatchReferences(platformId, files, matches, requireMrpackHost)
    }
}

private fun ((Progress) -> Unit).inInterval(start: Double, end: Double): (Progress) -> Unit = { progress ->
    this(loading(start + (progress.toFractionOrNull() ?: 0f) * (end - start)))
}
