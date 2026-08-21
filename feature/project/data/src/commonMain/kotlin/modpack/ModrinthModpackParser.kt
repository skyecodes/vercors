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
                ModpackManifestFile(
                    targetPath = file.path,
                    kind = file.path.toModpackFileKind(),
                    downloadUrl = file.downloads.firstOrNull(),
                    sha1 = file.hashes.sha1,
                    size = file.fileSize,
                    providerProjectId = null,
                    providerFileId = null
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

private fun String.toModLoaderTypeOrNull(): ModLoaderType? = when (this) {
    "forge" -> ModLoaderType.Forge
    "neoforge" -> ModLoaderType.NeoForge
    "fabric-loader" -> ModLoaderType.Fabric
    "quilt-loader" -> ModLoaderType.Quilt
    else -> null
}
