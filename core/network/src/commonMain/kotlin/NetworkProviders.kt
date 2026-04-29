package app.vercors.core.network

import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
interface NetworkProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(@Named("metaApiUrl") metaApiUrl: String): HttpClient =
        HttpClient(platformHttpClientEngineFactory) {
            expectSuccess = true
            install(DefaultRequest) {
                url(metaApiUrl)
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(HttpRequestRetry)
        }
}

internal expect val platformHttpClientEngineFactory: HttpClientEngineFactory<*>
