package app.vercors.feature.game.data.repository

import app.vercors.core.filesystem.*
import app.vercors.core.network.RemoteError
import app.vercors.core.network.makeMetaApiRequest
import app.vercors.feature.game.api.Game
import app.vercors.feature.game.api.Loaders
import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.feature.game.data.toApiData
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.repository.LoaderRepository
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.AppPaths
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.raise.context.withError
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.flow
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import opensavvy.progress.loading
import opensavvy.spine.api.div
import opensavvy.spine.api.invoke
import opensavvy.spine.client.handle
import opensavvy.spine.client.request
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class LoaderRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json,
    private val appPaths: AppPaths,
) : LoaderRepository {

    override fun observeAvailableLoaders(gameVersion: String): ProgressiveFlow<LoaderError, List<ModLoaderType>> =
        flow {
            val cacheFile = appPaths.availableLoadersFile(gameVersion)
            val serializer = ListSerializer(LoaderTypeData.serializer())
            val cached = readCacheOrNull(cacheFile, serializer)
            emit(cached?.map { it.toDomain() }?.successfulWithProgress(loading()) ?: ProgressiveOutcome.Incomplete())

            either {
                withError(RemoteError::toLoaderError) {
                    makeMetaApiRequest {
                        httpClient.request(
                            endpoint = ApiV1 / Game / Game.GameVersionId(gameVersion) / Loaders / Loaders.list,
                            configure = { expectSuccess = false }
                        )
                    }
                }.handle(
                    handle1 = { raise(LoaderError(it.error, it.description)) },
                    transform = { it }
                )
            }.fold(
                ifRight = { list ->
                    writeCacheQuietly(cacheFile, serializer, list)
                    emit(list.map { it.toDomain() }.successfulWithProgress())
                },
                ifLeft = { error ->
                    if (cached == null) emit(error.failedWithProgress())
                    else logger.warn { "Failed to refresh available loaders for $gameVersion, keeping cached copy: $error" }
                }
        )
        }

    override fun observeLoaderVersions(
        gameVersion: String,
        loader: ModLoaderType
    ): ProgressiveFlow<LoaderError, List<LoaderVersion>> = flow {
        val cacheFile = appPaths.loaderVersionsFile(gameVersion, loader.wireId())
        val serializer = ListSerializer(LoaderVersionData.serializer())
        val cached = readCacheOrNull(cacheFile, serializer)
        emit(cached?.map { it.toDomain() }?.successfulWithProgress(loading()) ?: ProgressiveOutcome.Incomplete())

        either {
            withError(RemoteError::toLoaderError) {
                makeMetaApiRequest {
                    httpClient.request(
                        endpoint = ApiV1 / Game / Game.GameVersionId(gameVersion) / Loaders /
                                Loaders.LoaderId(loader.wireId()) / Loaders.LoaderId.versions,
                        configure = { expectSuccess = false }
                    )
                }
            }.handle(
                handle1 = { raise(LoaderError(it.error, it.description)) },
                transform = { it }
            )
        }.fold(
            ifRight = { list ->
                writeCacheQuietly(cacheFile, serializer, list)
                emit(list.map { it.toDomain() }.successfulWithProgress())
            },
            ifLeft = { error ->
                if (cached == null) emit(error.failedWithProgress())
                else logger.warn {
                    "Failed to refresh loader versions for $gameVersion/${loader.wireId()}, keeping cached copy: $error"
                }
            }
        )
    }

    private fun ModLoaderType.wireId(): String = json.encodeToJsonElement(toApiData()).jsonPrimitive.content

    private suspend fun <T> readCacheOrNull(cacheFile: Path, serializer: KSerializer<List<T>>): List<T>? {
        if (!cacheFile.exists()) return null
        return try {
            cacheFile.readJson(serializer, json)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.warn(e) { "Corrupt loader cache at $cacheFile, ignoring" }
            null
        }
    }

    private suspend fun <T> writeCacheQuietly(cacheFile: Path, serializer: KSerializer<List<T>>, value: List<T>) {
        val tempPath = Path("$cacheFile.tmp")
        try {
            tempPath.writeJson(serializer, value, json)
            tempPath.moveTo(cacheFile)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.warn(e) { "Failed to write loader cache to $cacheFile" }
        } finally {
            if (tempPath.exists()) tempPath.deleteRecursively()
        }
    }
}

private fun RemoteError.toLoaderError(): LoaderError = when (this) {
    RemoteError.Network -> LoaderError("network", "Could not connect to the server")
    RemoteError.Timeout -> LoaderError("timeout", "The server took too long to respond")
    RemoteError.Serialization -> LoaderError("serialization", "Received an unexpected response from the server")
    is RemoteError.Http -> LoaderError("http_$status", body)
}
