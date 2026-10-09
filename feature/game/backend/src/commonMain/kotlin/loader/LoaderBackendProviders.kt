package app.vercors.feature.game.backend.loader

import app.vercors.shared.backend.ApiResponseConverterFactory
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*

@ContributesTo(AppScope::class)
@BindingContainer
object LoaderBackendProviders {
    @SingleIn(AppScope::class)
    @Provides
    @Named(FABRIC_LOADER_ID)
    fun provideFabricMetaApi(httpClient: HttpClient): MetaLoaderApi =
        loaderKtorfit(httpClient, "$FABRIC_META_BASE_URL/").platformCreateMetaLoaderApi()

    @SingleIn(AppScope::class)
    @Provides
    @Named(QUILT_LOADER_ID)
    fun provideQuiltMetaApi(httpClient: HttpClient): MetaLoaderApi =
        loaderKtorfit(httpClient, "$QUILT_META_BASE_URL/").platformCreateMetaLoaderApi()

    @SingleIn(AppScope::class)
    @Provides
    @Named(FORGE_LOADER_ID)
    fun provideForgeMavenApi(httpClient: HttpClient): MavenApi =
        loaderKtorfit(httpClient, "$FORGE_MAVEN_BASE_URL/").platformCreateMavenApi()

    @SingleIn(AppScope::class)
    @Provides
    @Named(NEOFORGE_LOADER_ID)
    fun provideNeoForgeMavenApi(httpClient: HttpClient): MavenApi =
        loaderKtorfit(httpClient, "$NEOFORGE_MAVEN_BASE_URL/").platformCreateMavenApi()

    private fun loaderKtorfit(httpClient: HttpClient, baseUrl: String): Ktorfit = Ktorfit.Builder()
        .httpClient(httpClient)
        .converterFactories(ApiResponseConverterFactory(loaderExceptionHandler))
        .baseUrl(baseUrl)
        .build()
}

expect fun Ktorfit.platformCreateMetaLoaderApi(): MetaLoaderApi

expect fun Ktorfit.platformCreateMavenApi(): MavenApi
