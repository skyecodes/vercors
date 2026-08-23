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

    /** Minimal, no-retry, short-timeout client used only for [MetaApiConnectivityPoller]'s health pings —
     *  the main [provideHttpClient] client's retry/backoff would make the liveness probe itself slow to fail. */
    @SingleIn(AppScope::class)
    @Provides
    @Named("metaHealthCheckHttpClient")
    fun provideMetaHealthCheckHttpClient(@Named("metaApiUrl") metaApiUrl: String): HttpClient =
        HttpClient(platformHttpClientEngineFactory) {
            install(DefaultRequest) {
                url(metaApiUrl)
            }
            install(HttpRequestRetry) {
                maxRetries = 0
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 3000
            }
        }
}

internal expect val platformHttpClientEngineFactory: HttpClientEngineFactory<*>
