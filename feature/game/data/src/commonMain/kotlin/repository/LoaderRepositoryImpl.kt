package app.vercors.feature.game.data.repository

import app.vercors.feature.game.api.Game
import app.vercors.feature.game.api.Loaders
import app.vercors.feature.game.data.toApiData
import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.repository.LoaderRepository
import app.vercors.shared.api.ApiV1
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import opensavvy.spine.api.div
import opensavvy.spine.api.invoke
import opensavvy.spine.client.handle
import opensavvy.spine.client.request

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class LoaderRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json
) : LoaderRepository {

    context(_: Raise<LoaderError>)
    override suspend fun getAvailableLoaders(gameVersion: String): List<ModLoaderType> =
        httpClient.request(
            endpoint = ApiV1 / Game / Game.GameVersionId(gameVersion) / Loaders / Loaders.list
        ).handle(
            handle1 = { raise(LoaderError(it.error, it.description)) },
            transform = { list -> list.map { it.toDomain() } }
        )

    context(_: Raise<LoaderError>)
    override suspend fun getLoaderVersions(gameVersion: String, loader: ModLoaderType): List<LoaderVersion> =
        httpClient.request(
            endpoint = ApiV1 / Game / Game.GameVersionId(gameVersion) / Loaders /
                    Loaders.LoaderId(json.encodeToJsonElement(loader.toApiData()).jsonPrimitive.content) /
                    Loaders.LoaderId.versions
        ).handle(
            handle1 = { raise(LoaderError(it.error, it.description)) },
            transform = { list -> list.map { it.toDomain() } }
        )
}
