package app.vercors.meta.client

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.cache.*
import kotlinx.io.files.Path
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

@Module
@ComponentScan
class MetaClientModule

@Single
internal fun provideKtorfit(
    httpClient: HttpClient,
    appPath: Path,
    @Property("META_BASE_URL") metaBaseUrl: String
): Ktorfit = Ktorfit.Builder()
    .httpClient(httpClient.config {
        install(HttpCache) {
            platformHttpCache(appPath)
        }
    })
    .converterFactories(ApiResponseConverter())
    .baseUrl(metaBaseUrl)
    .build()


internal expect fun HttpCache.Config.platformHttpCache(appPath: Path)
