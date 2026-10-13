package app.vercors.feature.project.data.modpack

import app.vercors.feature.project.data.dto.MrpackIndex
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackManifestFile
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
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

private const val MODRINTH_PLATFORM_ID = "modrinth"

// modrinth.index.json has no per-file project/version IDs: recover them from the CDN URL when it matches
private val MODRINTH_CDN_URL_REGEX = Regex("""^https://cdn\.modrinth\.com/data/([^/]+)/versions/([^/]+)/[^/]+$""")

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(MODRINTH_PLATFORM_ID)
class ModrinthModpackParser(private val json: Json) : ModpackManifestParser {
    context(_: Raise<ModpackResolutionError>)
    override suspend fun parse(zipPath: Path, projectId: ProjectId, fileId: String): ModpackManifest {
        val index = readIndex(zipPath)
        val gameVersion = index.dependencies[MRPACK_MINECRAFT_KEY] ?: raise(ModpackResolutionError.ParseFailed)
        val (loaderType, loaderVersion) = index.dependencies.firstNotNullOfOrNull { (key, version) ->
            key.toMrpackModLoaderTypeOrNull()?.let { it to version }
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

    // .mrpack already embeds every file's metadata, so parse() resolves everything
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
    } catch (_: Exception) {
        raise(ModpackResolutionError.ParseFailed)
    }
}

private fun String.toModrinthCdnProviderIds(): Pair<String, String>? =
    MODRINTH_CDN_URL_REGEX.matchEntire(this)?.destructured?.let { (projectId, versionId) -> projectId to versionId }
