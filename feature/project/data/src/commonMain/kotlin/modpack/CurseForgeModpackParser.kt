package app.vercors.feature.project.data.modpack

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.data.dto.CurseForgeManifest
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackManifestFile
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.raise
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.StringKey
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private const val CURSEFORGE_PLATFORM_ID = "curseforge"
private const val RESOLVE_CONCURRENCY = 5

// Bounds request size while keeping most of the batching savings
private const val RESOLVE_BATCH_SIZE = 50

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(CURSEFORGE_PLATFORM_ID)
class CurseForgeModpackParser(
    private val json: Json,
    private val projectRepository: ProjectRepository
) : ModpackManifestParser {
    context(_: Raise<ModpackResolutionError>)
    override suspend fun parse(zipPath: Path, projectId: ProjectId, fileId: String): ModpackManifest {
        val manifest = readManifest(zipPath)
        val primaryLoader = manifest.minecraft.modLoaders.firstOrNull { it.primary }
            ?: manifest.minecraft.modLoaders.firstOrNull()
        val (loaderType, loaderVersion) = primaryLoader?.id?.toModLoaderTypeAndVersionOrNull() ?: (null to null)

        return ModpackManifest(
            platformId = projectId.platform,
            projectId = projectId.id,
            fileId = fileId,
            name = manifest.name,
            gameVersion = manifest.minecraft.version,
            loaderType = loaderType,
            loaderVersion = loaderVersion,
            // Resolved by resolveFiles: the manifest only lists project/file ID pairs
            files = emptyList(),
            overridesDirs = listOf(manifest.overrides)
        )
    }

    @OptIn(ExperimentalAtomicApi::class)
    context(_: Raise<ModpackResolutionError>)
    override suspend fun resolveFiles(
        zipPath: Path,
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit
    ): ModpackManifest {
        if (manifest.files.isNotEmpty()) {
            onProgress(done())
            return manifest
        }
        val entries = readManifest(zipPath).files
        onProgress(loading(0.0))
        val total = entries.size.toDouble()
        val resolved = AtomicInt(0)
        val chunks = entries.chunked(RESOLVE_BATCH_SIZE)
        val chunkResolutions = chunks.parMap(concurrency = RESOLVE_CONCURRENCY) { chunk ->
            either {
                projectRepository.getProjectFilesByIds(
                    CURSEFORGE_PLATFORM_ID,
                    chunk.map { it.fileID.toString() }
                )
            }.also { onProgress(loading(resolved.addAndFetch(chunk.size) / total)) }
        }
        // The batch endpoint can silently omit invalid IDs: look each one up so a bad entry still fails
        val filesById = chunkResolutions.flatMap { it.mapLeft { ModpackResolutionError.FileResolutionFailed }.bind() }
            .associateBy { it.id }
        val files = entries.map { entry ->
            val file = filesById[entry.fileID.toString()] ?: raise(ModpackResolutionError.FileResolutionFailed)
            val kind = (file.type ?: ProjectType.Mod).toModpackFileKind()
            ModpackManifestFile(
                targetPath = "${kind.toFolderName()}/${file.fileName}",
                kind = kind,
                downloadUrl = null,
                sha1 = file.sha1,
                md5 = file.md5,
                size = file.size,
                providerProjectId = entry.projectID.toString(),
                providerFileId = entry.fileID.toString(),
                manualDownloadUrl = file.manualDownloadUrl
            )
        }
        onProgress(done())
        return manifest.copy(files = files)
    }

    context(_: Raise<ModpackResolutionError>)
    private fun readManifest(zipPath: Path): CurseForgeManifest = try {
        Zip.open(zipPath).use { zip ->
            val text = zip.entry(Path("manifest.json")) { readToBytes() }.decodeToString()
            json.decodeFromString(CurseForgeManifest.serializer(), text)
        }
    } catch (_: Exception) {
        raise(ModpackResolutionError.ParseFailed)
    }
}

// CurseForge loader ids look like "forge-47.2.20"
private fun String.toModLoaderTypeAndVersionOrNull(): Pair<ModLoaderType, String>? {
    val (slug, version) = substringBefore('-') to substringAfter('-', missingDelimiterValue = "")
    if (version.isEmpty()) return null
    val type = when (slug) {
        "forge" -> ModLoaderType.Forge
        "neoforge" -> ModLoaderType.NeoForge
        "fabric" -> ModLoaderType.Fabric
        "quilt" -> ModLoaderType.Quilt
        else -> return null
    }
    return type to version
}
