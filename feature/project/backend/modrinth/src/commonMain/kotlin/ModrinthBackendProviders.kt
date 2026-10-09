package app.vercors.feature.project.backend.modrinth

import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.feature.project.backend.PlatformResponseConverterFactory
import app.vercors.feature.project.backend.defaultExceptionHandler
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import kotlinx.serialization.Serializable

@ContributesTo(AppScope::class)
@BindingContainer
object ModrinthBackendProviders {
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
