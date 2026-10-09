package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@SingleIn(AppScope::class)
@Inject
class LoaderService(
    private val loaderServices: Map<String, LoaderServiceBase>
) {
    context(_: Raise<LoaderErrorData>)
    suspend fun getAvailableLoaders(gameVersion: String): List<LoaderTypeData> = coroutineScope {
        loaderServices.entries.map { (id, service) ->
            async { id to service.getVersions(gameVersion) }
        }.awaitAll()
            .filter { (_, versions) -> versions.isNotEmpty() }
            .map { (id, _) -> id.toLoaderTypeData() }
    }

    context(_: Raise<LoaderErrorData>)
    suspend fun getLoaderVersions(gameVersion: String, loaderType: LoaderTypeData): List<LoaderVersionData> =
        serviceFor(loaderType).getVersions(gameVersion)
            .sortedWith(compareBy(versionComparator) { it.version })
            .asReversed()

    context(_: Raise<LoaderErrorData>)
    suspend fun getInstallUrl(gameVersion: String, loaderType: LoaderTypeData, loaderVersion: String): String =
        serviceFor(loaderType).getInstallUrl(gameVersion, loaderVersion)

    context(_: Raise<LoaderErrorData>)
    private fun serviceFor(loaderType: LoaderTypeData): LoaderServiceBase =
        ensureNotNull(loaderServices[loaderType.key]) {
            LoaderErrorData("UnknownLoader", "Unknown loader type: $loaderType")
        }
}
