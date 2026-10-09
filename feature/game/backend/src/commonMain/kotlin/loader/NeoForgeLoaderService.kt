package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.shared.backend.bindResponse
import arrow.core.raise.Raise
import dev.zacsweers.metro.*

internal const val NEOFORGE_LOADER_ID = "neoforge"
internal const val NEOFORGE_MAVEN_BASE_URL = "https://maven.neoforged.net"
private const val NEOFORGE_ARTIFACT_PATH = "net%2Fneoforged%2Fneoforge"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(NEOFORGE_LOADER_ID)
class NeoForgeLoaderService(
    @Named(NEOFORGE_LOADER_ID) private val api: MavenApi
) : LoaderServiceBase {
    context(_: Raise<LoaderErrorData>)
    override suspend fun getVersions(gameVersion: String): List<LoaderVersionData> {
        val prefix = "${gameVersion.removePrefix("1.")}."
        return api.getReleaseVersions(NEOFORGE_ARTIFACT_PATH).bindResponse().versions
            .filter { it.startsWith(prefix) }
            .map { LoaderVersionData(version = it, stable = !it.contains("-beta")) }
    }

    context(_: Raise<LoaderErrorData>)
    override suspend fun getInstallUrl(gameVersion: String, loaderVersion: String): String =
        "$NEOFORGE_MAVEN_BASE_URL/releases/net/neoforged/neoforge/$loaderVersion/neoforge-$loaderVersion-installer.jar"
}
