package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise
import dev.zacsweers.metro.*

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
}
