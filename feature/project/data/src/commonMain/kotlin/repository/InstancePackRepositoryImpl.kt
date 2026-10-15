package app.vercors.feature.project.data.repository

import app.vercors.core.filesystem.createDirectories
import app.vercors.core.filesystem.exists
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.export.InstanceExportError
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.project.data.dto.*
import app.vercors.feature.project.data.modpack.MRPACK_MINECRAFT_KEY
import app.vercors.feature.project.data.modpack.toModpackFileKind
import app.vercors.feature.project.data.modpack.toMrpackDependencyKey
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackManifestFile
import app.vercors.feature.project.domain.model.pack.PackFileReference
import app.vercors.feature.project.domain.model.pack.VercorsPackError
import app.vercors.feature.project.domain.model.pack.VercorsPackInfo
import app.vercors.feature.project.domain.repository.InstancePackRepository
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.ZipWriter
import app.vercors.shared.data.dto.toData
import app.vercors.shared.data.dto.toDomain
import app.vercors.shared.data.writeZipAtomically
import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.loading
import kotlin.uuid.Uuid

private const val MRPACK_INDEX_ENTRY = "modrinth.index.json"
private const val VERCORS_PACK_MANIFEST_ENTRY = "vercors.json"
private const val VERCORS_PACK_FORMAT_VERSION = 1
private const val VERCORS_PACK_PLATFORM_ID = "local"
private const val OVERRIDES_DIR = "overrides"
private const val PACK_ICON_NAME = "icon"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstancePackRepositoryImpl(
    private val json: Json,
    private val appPaths: AppPaths,
) : InstancePackRepository {
    context(_: Raise<InstanceExportError>)
    override suspend fun writeMrpack(
        instance: Instance,
        references: List<PackFileReference>,
        overrides: List<InstanceExportFile>,
        versionId: String,
        summary: String?,
        targetPath: String,
        onProgress: (Progress) -> Unit
    ) {
        val index = MrpackIndex(
            versionId = versionId,
            name = instance.name,
            summary = summary?.takeIf { it.isNotBlank() },
            files = references.map { it.toMrpackFile() },
            dependencies = instance.toMrpackDependencies()
        )
        val indexJson = json.encodeToString(MrpackIndex.serializer(), index)
        orRaiseWriteFailed {
            Path(targetPath).writeZipAtomically {
                addBytes(MRPACK_INDEX_ENTRY, indexJson.encodeToByteArray())
                addOverrides(overrides, onProgress)
            }
        }
    }

    context(_: Raise<InstanceExportError>)
    override suspend fun writeVercorsPack(
        instance: Instance,
        references: List<PackFileReference>,
        overrides: List<InstanceExportFile>,
        targetPath: String,
        onProgress: (Progress) -> Unit
    ) {
        val iconPath = instance.iconFilePath?.let(::Path)?.takeIf { it.exists() }
        val iconExtension = iconPath?.name?.substringAfterLast('.', "")?.takeIf { it.isNotEmpty() }
        val manifest = VercorsPackManifest(
            formatVersion = VERCORS_PACK_FORMAT_VERSION,
            name = instance.name,
            gameVersion = instance.gameVersion,
            modLoader = instance.modLoader?.let { VercorsPackModLoader(it.type.name, it.version) },
            modpackInfo = instance.modpackInfo?.let {
                VercorsPackModpackInfo(
                    it.platformId,
                    it.projectId,
                    it.fileId,
                    it.versionName
                )
            },
            jvmMemoryOptions = instance.jvmMemoryOptions.toData(),
            // Library paths only exist on the exporting machine
            nativeLibraryOptions = instance.nativeLibraryOptions.copy(glfwPath = null, openAlPath = null).toData(),
            iconExtension = iconExtension,
            files = references.map { it.toVercorsPackFile() }
        )
        val manifestJson = json.encodeToString(VercorsPackManifest.serializer(), manifest)
        orRaiseWriteFailed {
            Path(targetPath).writeZipAtomically {
                addBytes(VERCORS_PACK_MANIFEST_ENTRY, manifestJson.encodeToByteArray())
                if (iconPath != null && iconExtension != null) addFile("$PACK_ICON_NAME.$iconExtension", iconPath)
                addOverrides(overrides, onProgress)
            }
        }
    }

    context(_: Raise<VercorsPackError>)
    override suspend fun parseVercorsPack(sourcePath: String): VercorsPackInfo {
        val text = try {
            Zip.open(Path(sourcePath)).use { zip ->
                zip.entryOrNull(Path(VERCORS_PACK_MANIFEST_ENTRY)) { readToBytes().decodeToString() }
            }
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            null
        } ?: raise(VercorsPackError.NotVercorsFormat)
        val manifest = try {
            json.decodeFromString(VercorsPackManifest.serializer(), text)
        } catch (_: Exception) {
            raise(VercorsPackError.ParseFailed)
        }
        ensure(manifest.formatVersion <= VERCORS_PACK_FORMAT_VERSION) { VercorsPackError.UnsupportedFormatVersion }
        ensure(manifest.files.all { it.path.isSafeRelativePath() }) { VercorsPackError.ParseFailed }
        val modLoader = manifest.modLoader?.let { loader ->
            val type = ModLoaderType.entries.firstOrNull { it.name.equals(loader.type, ignoreCase = true) }
                ?: raise(VercorsPackError.ParseFailed)
            InstanceModLoader(type, loader.version)
        }
        return VercorsPackInfo(
            name = manifest.name,
            gameVersion = manifest.gameVersion,
            modLoader = modLoader,
            modpackInfo = manifest.modpackInfo?.let {
                InstanceModpackInfo(
                    it.platformId,
                    it.projectId,
                    it.fileId,
                    it.versionName
                )
            },
            jvmMemoryOptions = manifest.jvmMemoryOptions.toDomain(),
            nativeLibraryOptions = manifest.nativeLibraryOptions.toDomain(),
            iconExtension = manifest.iconExtension,
            manifest = ModpackManifest(
                platformId = VERCORS_PACK_PLATFORM_ID,
                projectId = VERCORS_PACK_PLATFORM_ID,
                fileId = VERCORS_PACK_PLATFORM_ID,
                name = manifest.name,
                gameVersion = manifest.gameVersion,
                loaderType = modLoader?.type,
                loaderVersion = modLoader?.version,
                files = manifest.files.map { it.toManifestFile() },
                overridesDirs = listOf(OVERRIDES_DIR)
            )
        )
    }

    override suspend fun extractVercorsPackIcon(sourcePath: String, iconExtension: String): String? = try {
        val target = Path(appPaths.downloadsTmp, "vcpack-icon-${Uuid.random()}.$iconExtension")
        appPaths.downloadsTmp.createDirectories()
        Zip.open(Path(sourcePath)).use { zip ->
            zip.entryOrNull(Path("$PACK_ICON_NAME.$iconExtension")) {
                readToPath(target)
                target.toString()
            }
        }
    } catch (_: Exception) {
        currentCoroutineContext().ensureActive()
        null
    }
}

private suspend fun ZipWriter.addOverrides(files: List<InstanceExportFile>, onProgress: (Progress) -> Unit) {
    files.forEachIndexed { index, file ->
        currentCoroutineContext().ensureActive()
        addFile("$OVERRIDES_DIR/${file.relativePath}", Path(file.path))
        onProgress(loading((index + 1).toDouble() / files.size))
    }
}

context(_: Raise<InstanceExportError>)
private suspend inline fun orRaiseWriteFailed(block: () -> Unit): Unit = try {
    block()
} catch (_: Exception) {
    currentCoroutineContext().ensureActive()
    raise(InstanceExportError.WriteFailed)
}

private fun String.isSafeRelativePath(): Boolean =
    isNotBlank() && !startsWith('/') && !contains('\\') && !contains(':') && split('/').none { it == ".." }

private fun Instance.toMrpackDependencies(): Map<String, String> = buildMap {
    put(MRPACK_MINECRAFT_KEY, gameVersion)
    modLoader?.let { put(it.type.toMrpackDependencyKey(), it.version) }
}

private fun PackFileReference.toMrpackFile(): MrpackFile = MrpackFile(
    path = relativePath,
    hashes = MrpackHashes(sha1 = sha1, sha512 = sha512),
    env = MrpackEnv(),
    downloads = listOfNotNull(downloadUrl),
    fileSize = size
)

private fun PackFileReference.toVercorsPackFile(): VercorsPackFile = VercorsPackFile(
    path = relativePath,
    platformId = platformId,
    projectId = projectId,
    fileId = fileId,
    sha1 = sha1,
    size = size
)

private fun VercorsPackFile.toManifestFile(): ModpackManifestFile = ModpackManifestFile(
    targetPath = path,
    kind = path.toModpackFileKind(),
    downloadUrl = null,
    sha1 = sha1,
    size = size,
    providerProjectId = projectId,
    providerFileId = fileId,
    platformId = platformId
)
