package app.vercors.feature.project.backend.modrinth

import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.feature.project.backend.PlatformResponseConverterFactory
import app.vercors.feature.project.backend.defaultExceptionHandler
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.serialization.Serializable

@ContributesTo(AppScope::class)
interface ModrinthBackendProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideModrinthApi(
        httpClient: HttpClient
    ): ModrinthApi = Ktorfit.Builder()
        .httpClient(httpClient)
        .converterFactories(PlatformResponseConverterFactory {
            when (it) {
                is ResponseException -> it.response.body<ModrinthError>().toPlatform()
                else -> defaultExceptionHandler(it)
            }
        })
        .baseUrl("https://api.modrinth.com/")
        .build()
        .platformCreateModrinthApi()
}

@Serializable
data class ModrinthError(
    val error: String,
    val description: String?
) {
    fun toPlatform(): PlatformErrorData = PlatformErrorData(error, description)
}

expect fun Ktorfit.platformCreateModrinthApi(): ModrinthApi
