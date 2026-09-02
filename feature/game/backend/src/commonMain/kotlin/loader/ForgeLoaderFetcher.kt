package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

private const val FORGE_MAVEN_BASE_URL = "https://maven.minecraftforge.net"
private const val FORGE_MAVEN_VERSIONS_URL =
    "$FORGE_MAVEN_BASE_URL/api/maven/versions/releases/net%2Fminecraftforge%2Fforge"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("forge")
@Inject
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

/**
 * Forge's maven build number is always shaped `\d+(\.\d+)+` and never contains `-`, so any qualifier suffix a
 * build's maven artifact-version adds after it (e.g. the game version repeated, as in "10.13.4.1614-1.7.10", or
 * an arbitrary qualifier like "10.13.3.1389-1710ls") is reliably delimited by the first `-`. A no-op on an
 * already-bare build number.
 */
private fun String.bareForgeBuildNumber(): String = substringBefore('-')

/**
 * Resolves [loaderVersion] (either a bare build number, e.g. from a CurseForge modpack manifest, or a full raw
 * suffixed remainder already round-tripped through [ForgeLoaderFetcher.getVersions]) against [rawVersions] (raw,
 * unstripped maven artifact-version strings for [gameVersion]) to the true maven artifact-version string —
 * instead of assuming it's always exactly "$gameVersion-$loaderVersion", which 404s for builds with a suffix.
 */
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
