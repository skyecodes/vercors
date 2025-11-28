package app.vercors.launcher.common.data

import app.vercors.meta.client.ApiResponseConverter
import ca.gosyer.appdirs.AppDirs
import de.jensklingenberg.ktorfit.Ktorfit
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.operations.Hasher
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.core.annotation.*

@Module
@ComponentScan
@Configuration
class CommonDataModule

@Qualifier
annotation class IODispatcher

@Single
@IODispatcher
internal fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

@Qualifier
annotation class MainDispatcher

@Single
@MainDispatcher
internal fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

@Qualifier
annotation class DefaultDispatcher

@Single
@DefaultDispatcher
internal fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

@Single
internal fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

@Single
internal fun provideAppDirs(): AppDirs = AppDirs {
    appName = "vercors-launcher"
}

@Qualifier
annotation class AppPath

@Single
@AppPath
internal fun provideAppPath(appDirs: AppDirs): Path = Path(appDirs.getUserDataDir())

@Factory
internal fun provideDirectoryWatcher(
    coroutineScope: CoroutineScope,
    @IODispatcher ioDispatcher: CoroutineDispatcher
): KfsDirectoryWatcher = KfsDirectoryWatcher(coroutineScope, ioDispatcher)

@Single
internal fun provideHttpClient(
    json: Json
): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
    install(HttpCache)
    install(HttpRequestRetry)
}

@Single
internal fun provideKtorfit(httpClient: HttpClient): Ktorfit = Ktorfit.Builder()
    .httpClient(httpClient)
    .converterFactories(ApiResponseConverter())
    .baseUrl("http://localhost:8080/")
    .build()

@Qualifier
annotation class Sha256

@Single
@Sha256
internal fun provideSha256Hasher(): Hasher = CryptographyProvider.Default.get(SHA256).hasher()