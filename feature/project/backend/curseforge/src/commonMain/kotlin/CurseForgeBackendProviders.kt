package app.vercors.feature.project.backend.curseforge

import app.vercors.feature.project.backend.PlatformResponseConverterFactory
import app.vercors.shared.backend.platformEnv
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*

@ContributesTo(AppScope::class)
interface CurseForgeBackendProviders {
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

expect fun Ktorfit.platformCreateCurseForgeApi(): CurseForgeApi
