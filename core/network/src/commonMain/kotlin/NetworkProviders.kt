package app.vercors.core.network

import app.vercors.core.domain.AppConstants
import dev.zacsweers.metro.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration

internal const val META_HEALTH_CHECK_HTTP_CLIENT = "metaHealthCheckHttpClient"

@ContributesTo(AppScope::class)
@BindingContainer
object NetworkProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(
        @Named(AppConstants.META_API_URL_KEY) metaApiUrl: String,
        @Named(AppConstants.NETWORK_TIMEOUT_KEY) networkTimeout: Duration
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
    @Named(META_HEALTH_CHECK_HTTP_CLIENT)
    fun provideMetaHealthCheckHttpClient(@Named(AppConstants.META_API_URL_KEY) metaApiUrl: String): HttpClient =
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
