package app.vercors.shared.data

import ca.gosyer.appdirs.AppDirs
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.operations.Hasher
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.annotation.*

@Module
@ComponentScan
class SharedDataModule

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

@Single
internal fun provideAppPath(appDirs: AppDirs): Path = Path(appDirs.getUserDataDir())

@Factory
internal fun provideDirectoryWatcher(
    coroutineScope: CoroutineScope,
    @IODispatcher ioDispatcher: CoroutineDispatcher
): KfsDirectoryWatcher = KfsDirectoryWatcher(coroutineScope, ioDispatcher)

@Single
internal fun provideHttpClient(
    json: Json
): HttpClient = HttpClient(platformHttpClientEngineFactory) {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json)
    }
    install(HttpRequestRetry)
}

@Qualifier
annotation class Sha1

@OptIn(DelicateCryptographyApi::class)
@Single
@Sha1
internal fun provideSha1Hasher(): Hasher = CryptographyProvider.Default.get(SHA1).hasher()

@Qualifier
annotation class Sha256

@Single
@Sha256
internal fun provideSha256Hasher(): Hasher = CryptographyProvider.Default.get(SHA256).hasher()

fun KoinApplication.commonDataProperties() {
    properties(
        mapOf(
            "MICROSOFT_CLIENT_ID" to MICROSOFT_CLIENT_ID,
            "META_BASE_URL" to META_BASE_URL
        )
    )
}

internal expect val platformHttpClientEngineFactory: HttpClientEngineFactory<*>
