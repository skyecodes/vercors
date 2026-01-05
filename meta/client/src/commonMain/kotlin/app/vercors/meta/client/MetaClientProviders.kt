package app.vercors.meta.client

import app.vercors.meta.client.game.GameApi
import app.vercors.meta.client.game.createGameApi
import app.vercors.meta.client.project.ProjectApi
import app.vercors.meta.client.project.createProjectApi
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.plugins.cache.*
import kotlinx.io.files.Path

@ContributesTo(AppScope::class)
interface MetaClientProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideKtorfit(
        httpClient: HttpClient,
        appPath: Path,
        @Named("metaBaseUrl") metaBaseUrl: String
    ): Ktorfit = Ktorfit.Builder()
        .httpClient(httpClient.config {
            install(HttpCache) {
                platformHttpCache(appPath)
            }
        })
        .converterFactories(ApiResponseConverter())
        .baseUrl(metaBaseUrl)
        .build()

    @SingleIn(AppScope::class)
    @Provides
    fun provideProjectApi(ktorfit: Ktorfit): ProjectApi = ktorfit.createProjectApi()

    @SingleIn(AppScope::class)
    @Provides
    fun provideGameApi(ktorfit: Ktorfit): GameApi = ktorfit.createGameApi()
}

internal expect fun HttpCache.Config.platformHttpCache(appPath: Path)
