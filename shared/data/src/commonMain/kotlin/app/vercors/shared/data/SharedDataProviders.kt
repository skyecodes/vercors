package app.vercors.shared.data

import ca.gosyer.appdirs.AppDirs
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.operations.Hasher
import dev.zacsweers.metro.*
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
interface SharedDataProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDirs(): AppDirs = AppDirs {
        appName = "vercors-launcher"
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppPath(appDirs: AppDirs): Path = Path(appDirs.getUserDataDir())

    @SingleIn(AppScope::class)
    @Provides
    fun provideDirectoryWatcher(coroutineScope: CoroutineScope): KfsDirectoryWatcher = KfsDirectoryWatcher(
        scope = coroutineScope,
        dispatcher = Dispatchers.IO
    )

    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(
        json: Json
    ): HttpClient = HttpClient(platformHttpClientEngineFactory) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpRequestRetry)
    }

    @OptIn(DelicateCryptographyApi::class)
    @SingleIn(AppScope::class)
    @Named("sha1")
    @Provides
    fun provideSha1Hasher(): Hasher = CryptographyProvider.Default.get(SHA1).hasher()

    @SingleIn(AppScope::class)
    @Named("sha256")
    @Provides
    fun provideSha256Hasher(): Hasher = CryptographyProvider.Default.get(SHA256).hasher()

    @SingleIn(AppScope::class)
    @Named("metaBaseUrl")
    @Provides
    fun provideMetaBaseUrl(): String = META_BASE_URL
}

internal expect val platformHttpClientEngineFactory: HttpClientEngineFactory<*>
