package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.feature.game.backend.loader.dto.MavenVersionsResponse
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.StringKey
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

internal const val FORGE_LOADER_ID = "forge"
private const val FORGE_MAVEN_BASE_URL = "https://maven.minecraftforge.net"
private const val FORGE_MAVEN_VERSIONS_URL =
    "$FORGE_MAVEN_BASE_URL/api/maven/versions/releases/net%2Fminecraftforge%2Fforge"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(FORGE_LOADER_ID)
class ForgeLoaderFetcher(
    private val httpClient: HttpClient
) : LoaderFetcher {
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
        val response = loaderRequest {
            httpClient.get(FORGE_MAVEN_VERSIONS_URL).body<MavenVersionsResponse>()
        }
        val prefix = "$gameVersion-"
        return response.versions.filter { it.startsWith(prefix) }
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
