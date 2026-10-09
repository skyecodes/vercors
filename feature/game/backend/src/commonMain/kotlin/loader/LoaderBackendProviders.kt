package app.vercors.feature.game.backend.loader

import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.*
import io.ktor.client.*

@ContributesTo(AppScope::class)
@BindingContainer
object LoaderBackendProviders {
    @SingleIn(AppScope::class)
    @Provides
    @Named(FABRIC_LOADER_ID)
    fun provideFabricMetaApi(httpClient: HttpClient): MetaLoaderApi = Ktorfit.Builder()
        .httpClient(httpClient)
        .baseUrl("$FABRIC_META_BASE_URL/")
        .build()
        .platformCreateMetaLoaderApi()

    @SingleIn(AppScope::class)
    @Provides
    @Named(QUILT_LOADER_ID)
    fun provideQuiltMetaApi(httpClient: HttpClient): MetaLoaderApi = Ktorfit.Builder()
        .httpClient(httpClient)
        .baseUrl("$QUILT_META_BASE_URL/")
        .build()
        .platformCreateMetaLoaderApi()
}

expect fun Ktorfit.platformCreateMetaLoaderApi(): MetaLoaderApi
