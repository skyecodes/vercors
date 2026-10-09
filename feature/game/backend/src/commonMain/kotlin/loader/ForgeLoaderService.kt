package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.shared.backend.bindResponse
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.*

internal const val FORGE_LOADER_ID = "forge"
internal const val FORGE_MAVEN_BASE_URL = "https://maven.minecraftforge.net"
private const val FORGE_ARTIFACT_PATH = "net%2Fminecraftforge%2Fforge"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(FORGE_LOADER_ID)
class ForgeLoaderService(
    @Named(FORGE_LOADER_ID) private val api: MavenApi
) : LoaderServiceBase {
    context(_: Raise<LoaderErrorData>)
    override suspend fun getVersions(gameVersion: String): List<LoaderVersionData> {
        val prefix = "$gameVersion-"
        return fetchRawVersions(gameVersion)
            .map { raw -> LoaderVersionData(version = raw.removePrefix(prefix).bareForgeBuildNumber(), stable = true) }
    }

    context(_: Raise<LoaderErrorData>)
    override suspend fun getInstallUrl(gameVersion: String, loaderVersion: String): String {
        val rawVersions = fetchRawVersions(gameVersion)
        val artifactVersion = ensureNotNull(resolveForgeArtifactVersion(gameVersion, loaderVersion, rawVersions)) {
            LoaderErrorData(
                error = "UnknownLoaderVersion",
                description = "No Forge $gameVersion artifact found for loader version $loaderVersion"
            )
        }
        return "$FORGE_MAVEN_BASE_URL/net/minecraftforge/forge/$artifactVersion/forge-$artifactVersion-installer.jar"
    }

    context(_: Raise<LoaderErrorData>)
    private suspend fun fetchRawVersions(gameVersion: String): List<String> {
        val prefix = "$gameVersion-"
        return api.getReleaseVersions(FORGE_ARTIFACT_PATH).bindResponse().versions.filter { it.startsWith(prefix) }
    }
}

// Forge build numbers never contain '-', so the first '-' delimits any qualifier suffix
private fun String.bareForgeBuildNumber(): String = substringBefore('-')

// Some builds carry a suffix, so "$gameVersion-$loaderVersion" isn't always the real artifact version
internal fun resolveForgeArtifactVersion(
    gameVersion: String,
    loaderVersion: String,
    rawVersions: List<String>
): String? {
    val prefix = "$gameVersion-"
    return rawVersions.firstOrNull { raw ->
        val remainder = raw.removePrefix(prefix)
        remainder == loaderVersion || remainder.bareForgeBuildNumber() == loaderVersion
    }
}
