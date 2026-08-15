package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

private const val FORGE_MAVEN_VERSIONS_URL =
    "https://maven.minecraftforge.net/api/maven/versions/releases/net%2Fminecraftforge%2Fforge"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("forge")
@Inject
class ForgeLoaderFetcher(
    private val httpClient: HttpClient
) : LoaderFetcher {
    context(_: Raise<LoaderErrorData>)
    override suspend fun getVersions(gameVersion: String): List<LoaderVersionData> {
        val response = loaderRequest {
            httpClient.get(FORGE_MAVEN_VERSIONS_URL).body<MavenVersionsResponse>()
        }
        val prefix = "$gameVersion-"
        return response.versions
            .filter { it.startsWith(prefix) }
            .map { LoaderVersionData(version = it.removePrefix(prefix), stable = true) }
    }
}
