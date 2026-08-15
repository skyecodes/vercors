package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

private const val NEOFORGE_MAVEN_VERSIONS_URL =
    "https://maven.neoforged.net/api/maven/versions/releases/net%2Fneoforged%2Fneoforge"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("neoforge")
@Inject
class NeoForgeLoaderFetcher(
    private val httpClient: HttpClient
) : LoaderFetcher {
    context(_: Raise<LoaderErrorData>)
    override suspend fun getVersions(gameVersion: String): List<LoaderVersionData> {
        val response = loaderRequest {
            httpClient.get(NEOFORGE_MAVEN_VERSIONS_URL).body<MavenVersionsResponse>()
        }
        val prefix = "${gameVersion.removePrefix("1.")}."
        return response.versions
            .filter { it.startsWith(prefix) }
            .map { LoaderVersionData(version = it, stable = !it.contains("-beta")) }
    }
}
