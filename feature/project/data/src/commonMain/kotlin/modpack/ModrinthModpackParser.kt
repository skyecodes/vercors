package app.vercors.feature.project.data.modpack

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackManifestFile
import app.vercors.feature.project.domain.model.modpack.ModpackParseFailed
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.*
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.done

private const val MODRINTH_PLATFORM_ID = "modrinth"

/**
 * Modrinth's CDN download URLs conventionally encode the project and version IDs directly, e.g.
 * `https://cdn.modrinth.com/data/<projectId>/versions/<versionId>/<fileName>` — `modrinth.index.json` itself
 * carries no per-file project/version ID field, so this is the only way to recover them without an API call.
 * Not every download URL follows this shape (mirrors, CDN changes, etc.), so entries that don't match are left
 * without a provider rather than guessed at.
 */
private val MODRINTH_CDN_URL_REGEX = Regex("""^https://cdn\.modrinth\.com/data/([^/]+)/versions/([^/]+)/[^/]+$""")

@Serializable
private data class MrpackIndex(
    val name: String,
    val files: List<MrpackFile> = emptyList(),
    val dependencies: Map<String, String> = emptyMap()
)

@Serializable
private data class MrpackFile(
    val path: String,
    val hashes: MrpackHashes = MrpackHashes(),
    val env: MrpackEnv? = null,
    val downloads: List<String> = emptyList(),
    val fileSize: Long? = null
)

@Serializable
private data class MrpackHashes(val sha1: String? = null)

@Serializable
private data class MrpackEnv(val client: String = "required", val server: String = "required")

/** Parses a Modrinth `.mrpack` — a zip containing `modrinth.index.json` plus `overrides`/`client-overrides` folders. */
@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(MODRINTH_PLATFORM_ID)
@Inject
class ModrinthModpackParser(private val json: Json) : ModpackManifestParser {
    context(_: Raise<ModpackResolutionError>)
    override suspend fun parse(zipPath: Path, projectId: ProjectId, fileId: String): ModpackManifest {
        val index = readIndex(zipPath)
        val gameVersion = index.dependencies["minecraft"] ?: raise(ModpackParseFailed)
        val (loaderType, loaderVersion) = index.dependencies.firstNotNullOfOrNull { (key, version) ->
            key.toModLoaderTypeOrNull()?.let { it to version }
        } ?: (null to null)

        val files = index.files
            .filterNot { it.env?.client == "unsupported" }
            .map { file ->
                val downloadUrl = file.downloads.firstOrNull()
                val providerIds = downloadUrl?.toModrinthCdnProviderIds()
                ModpackManifestFile(
                    targetPath = file.path,
                    kind = file.path.toModpackFileKind(),
                    downloadUrl = downloadUrl,
                    sha1 = file.hashes.sha1,
                    size = file.fileSize,
                    providerProjectId = providerIds?.first,
                    providerFileId = providerIds?.second
                )
            }

        return ModpackManifest(
            platformId = projectId.platform,
            projectId = projectId.id,
            fileId = fileId,
            name = index.name,
            gameVersion = gameVersion,
            loaderType = loaderType,
            loaderVersion = loaderVersion,
            files = files,
            overridesDirs = listOf("overrides", "client-overrides")
        )
    }

    // .mrpack files already embed each entry's filename/hash/download URL, so parse() resolves everything upfront.
    context(_: Raise<ModpackResolutionError>)
    override suspend fun resolveFiles(
        zipPath: Path,
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit
    ): ModpackManifest {
        onProgress(done())
        return manifest
    }

    context(_: Raise<ModpackResolutionError>)
    private fun readIndex(zipPath: Path): MrpackIndex = try {
        Zip.open(zipPath).use { zip ->
            val text = zip.entry(Path("modrinth.index.json")) { readToBytes() }.decodeToString()
            json.decodeFromString(MrpackIndex.serializer(), text)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        raise(ModpackParseFailed)
    }
}

private fun String.toModrinthCdnProviderIds(): Pair<String, String>? =
    MODRINTH_CDN_URL_REGEX.matchEntire(this)?.destructured?.let { (projectId, versionId) -> projectId to versionId }

private fun String.toModLoaderTypeOrNull(): ModLoaderType? = when (this) {
    "forge" -> ModLoaderType.Forge
    "neoforge" -> ModLoaderType.NeoForge
    "fabric-loader" -> ModLoaderType.Fabric
    "quilt-loader" -> ModLoaderType.Quilt
    else -> null
}
