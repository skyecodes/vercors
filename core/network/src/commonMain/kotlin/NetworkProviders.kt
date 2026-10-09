package app.vercors.core.network

import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration

@ContributesTo(AppScope::class)
@BindingContainer
object NetworkProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(
        @Named("metaApiUrl") metaApiUrl: String,
        @Named("networkTimeout") networkTimeout: Duration
    ): HttpClient =
        HttpClient(platformHttpClientEngineFactory) {
            expectSuccess = true
            installPlatformTrustStore()
            install(DefaultRequest) {
                url(metaApiUrl)
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(2)
                exponentialDelay()
            }
            install(HttpCache)
            install(HttpTimeout) {
                val networkTimeoutMillis = networkTimeout.inWholeMilliseconds
                connectTimeoutMillis = networkTimeoutMillis
                socketTimeoutMillis = networkTimeoutMillis
            }
        }

    // No retry/backoff, so the liveness probe itself fails fast
    @SingleIn(AppScope::class)
    @Provides
    @Named("metaHealthCheckHttpClient")
    fun provideMetaHealthCheckHttpClient(@Named("metaApiUrl") metaApiUrl: String): HttpClient =
        HttpClient(platformHttpClientEngineFactory) {
            installPlatformTrustStore()
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

internal expect fun <T : HttpClientEngineConfig> HttpClientConfig<T>.installPlatformTrustStore()
