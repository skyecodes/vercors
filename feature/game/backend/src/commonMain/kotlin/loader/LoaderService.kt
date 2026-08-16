package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import arrow.core.raise.recover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@Inject
class LoaderService(
    private val fetchers: Map<String, LoaderFetcher>
) {
    suspend fun getAvailableLoaders(gameVersion: String): List<LoaderTypeData> = coroutineScope {
        fetchers.entries.map { (id, fetcher) ->
            async {
                id to recover({ fetcher.getVersions(gameVersion) }) { error ->
                    logger.warn { "Failed to fetch $id loader versions for $gameVersion: $error" }
                    emptyList()
                }
            }
        }.awaitAll()
            .filter { (_, versions) -> versions.isNotEmpty() }
            .map { (id, _) -> id.toLoaderTypeData() }
    }

    context(_: Raise<LoaderErrorData>)
    suspend fun getLoaderVersions(gameVersion: String, loaderType: LoaderTypeData): List<LoaderVersionData> {
        val fetcher = ensureNotNull(fetchers[loaderType.key]) {
            LoaderErrorData("UnknownLoader", "Unknown loader type: $loaderType")
        }
        return fetcher.getVersions(gameVersion).sortedWith(compareBy(versionComparator) { it.version }).asReversed()
    }

    context(_: Raise<LoaderErrorData>)
    suspend fun getInstallUrl(gameVersion: String, loaderType: LoaderTypeData, loaderVersion: String): String {
        val fetcher = ensureNotNull(fetchers[loaderType.key]) {
            LoaderErrorData("UnknownLoader", "Unknown loader type: $loaderType")
        }
        return fetcher.getInstallUrl(gameVersion, loaderVersion)
    }
}
