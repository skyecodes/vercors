package app.vercors.feature.project.backend

import app.vercors.feature.project.api.dto.PlatformErrorData
import app.vercors.feature.project.backend.curseforge.CurseForgeApi
import app.vercors.feature.project.backend.modrinth.ModrinthApi
import app.vercors.shared.backend.platformEnv
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@ContributesTo(AppScope::class)
interface ProjectBackendProviders {
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

    @SingleIn(AppScope::class)
    @Provides
    fun provideCurseForgeApi(
        httpClient: HttpClient,
        @Named("CURSEFORGE_API_KEY") curseForgeApiKey: String
    ): CurseForgeApi = Ktorfit.Builder()
        .httpClient(httpClient.config {
            defaultRequest {
                header("x-api-key", curseForgeApiKey)
            }
        })
        .converterFactories(PlatformResponseConverterFactory())
        .baseUrl("https://api.curseforge.com/")
        .build()
        .platformCreateCurseForgeApi()

    @Provides
    @Named("CURSEFORGE_API_KEY")
    fun provideCurseforgeApiKey(): String =
        requireNotNull(platformEnv("CURSEFORGE_API_KEY")) { "CURSEFORGE_API_KEY environment variable must be set" }
}

@Serializable
data class ModrinthError(
    val error: String,
    val description: String?
) {
    fun toPlatform(): PlatformErrorData = PlatformErrorData(error, description)
}

expect fun Ktorfit.platformCreateModrinthApi(): ModrinthApi

expect fun Ktorfit.platformCreateCurseForgeApi(): CurseForgeApi
