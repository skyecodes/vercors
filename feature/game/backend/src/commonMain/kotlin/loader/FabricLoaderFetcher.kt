package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise
import dev.zacsweers.metro.*

private const val FABRIC_META_BASE_URL = "https://meta.fabricmc.net/v2"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey("fabric")
@Inject
class FabricLoaderFetcher(
    @Named("fabric") private val api: MetaLoaderApi
) : LoaderFetcher {
    context(_: Raise<LoaderErrorData>)
    override suspend fun getVersions(gameVersion: String): List<LoaderVersionData> =
        loaderRequest { api.getLoaderVersions(gameVersion) }
            .map { LoaderVersionData(version = it.loader.version, stable = it.loader.effectiveStable) }

    context(_: Raise<LoaderErrorData>)
    override suspend fun getInstallUrl(gameVersion: String, loaderVersion: String): String =
        "$FABRIC_META_BASE_URL/versions/loader/$gameVersion/$loaderVersion/profile/json"
}
