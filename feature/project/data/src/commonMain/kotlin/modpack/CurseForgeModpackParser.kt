package app.vercors.feature.project.data.modpack

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.*
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import arrow.core.raise.context.bind
import arrow.core.raise.context.raise
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.*
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

private const val CURSEFORGE_PLATFORM_ID = "curseforge"
private const val RESOLVE_CONCURRENCY = 5

@Serializable
private data class CurseForgeManifest(
    val minecraft: CurseForgeManifestMinecraft,
    val name: String,
    val files: List<CurseForgeManifestFile> = emptyList(),
    val overrides: String = "overrides"
)

@Serializable
private data class CurseForgeManifestMinecraft(
    val version: String,
    val modLoaders: List<CurseForgeManifestModLoader> = emptyList()
)

@Serializable
private data class CurseForgeManifestModLoader(val id: String, val primary: Boolean = false)

@Serializable
private data class CurseForgeManifestFile(val projectID: Long, val fileID: Long, val required: Boolean = true)

/**
 * Parses a CurseForge modpack zip — `manifest.json` (a `{projectID, fileID}` list, no filenames/hashes) plus an
 * `overrides` folder. Each manifest entry is resolved to real file metadata via [ProjectRepository.getProjectFile],
 * bounded to [RESOLVE_CONCURRENCY] concurrent requests to avoid hammering CurseForge's API.
 */
@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(CURSEFORGE_PLATFORM_ID)
@Inject
class CurseForgeModpackParser(
    private val json: Json,
    // Lazy to break the dependency cycle: ProjectRepositoryImpl injects the parser map this contributes into.
    private val projectRepository: Lazy<ProjectRepository>
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
            // Resolved separately by resolveFiles — CurseForge's own manifest only lists project/file ID pairs,
            // each requiring its own network lookup to get a real filename/hash/download URL.
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
        val entries = readManifest(zipPath).files
        onProgress(loading(0.0))
        val total = entries.size.toDouble()
        val resolved = AtomicInt(0)
        val fileResolutions = entries.parMap(concurrency = RESOLVE_CONCURRENCY) { entry ->
            either {
                projectRepository.value.getProjectFile(
                    ProjectId(entry.projectID.toString(), CURSEFORGE_PLATFORM_ID),
                    entry.fileID.toString()
                )
            }.also { onProgress(loading(resolved.incrementAndFetch() / total)) }
        }
        val files = entries.zip(fileResolutions).map { (entry, resolution) ->
            val file = resolution.mapLeft { ModpackFileResolutionFailed }.bind()
            ModpackManifestFile(
                targetPath = "mods/${file.fileName}",
                kind = ModpackFileKind.Mod,
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
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        raise(ModpackParseFailed)
    }
}

/** CurseForge loader ids are `"<slug>-<version>"`, e.g. `"forge-47.2.20"`. */
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
