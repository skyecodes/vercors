package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.shared.backend.bindResponse
import arrow.core.raise.Raise
import dev.zacsweers.metro.*

internal const val QUILT_LOADER_ID = "quilt"
internal const val QUILT_META_BASE_URL = "https://meta.quiltmc.org/v3"

@SingleIn(AppScope::class)
@ContributesIntoMap(AppScope::class)
@StringKey(QUILT_LOADER_ID)
class QuiltLoaderService(
    @Named(QUILT_LOADER_ID) private val api: MetaLoaderApi
) : LoaderServiceBase {
    context(_: Raise<LoaderErrorData>)
    override suspend fun getVersions(gameVersion: String): List<LoaderVersionData> =
        api.getLoaderVersions(gameVersion).bindResponse()
            .map { LoaderVersionData(version = it.loader.version, stable = it.loader.effectiveStable) }

    context(_: Raise<LoaderErrorData>)
    override suspend fun getInstallUrl(gameVersion: String, loaderVersion: String): String =
        "$QUILT_META_BASE_URL/versions/loader/$gameVersion/$loaderVersion/profile/json"
}
