package app.vercors.feature.game.backend.loader

import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*

@ContributesTo(AppScope::class)
interface LoaderBackendProviders {
    @SingleIn(AppScope::class)
    @Provides
    @Named("fabric")
    fun provideFabricMetaApi(httpClient: HttpClient): MetaLoaderApi = Ktorfit.Builder()
        .httpClient(httpClient)
        .baseUrl("https://meta.fabricmc.net/v2/")
        .build()
        .platformCreateMetaLoaderApi()

    @SingleIn(AppScope::class)
    @Provides
    @Named("quilt")
    fun provideQuiltMetaApi(httpClient: HttpClient): MetaLoaderApi = Ktorfit.Builder()
        .httpClient(httpClient)
        .baseUrl("https://meta.quiltmc.org/v3/")
        .build()
        .platformCreateMetaLoaderApi()
}

expect fun Ktorfit.platformCreateMetaLoaderApi(): MetaLoaderApi
