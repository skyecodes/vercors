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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
interface SharedDataProviders {
    @SingleIn(AppScope::class)
    @Provides
    @IODispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @SingleIn(AppScope::class)
    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @SingleIn(AppScope::class)
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

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
    fun provideDirectoryWatcher(
        coroutineScope: CoroutineScope,
        @IODispatcher ioDispatcher: CoroutineDispatcher
    ): KfsDirectoryWatcher = KfsDirectoryWatcher(coroutineScope, ioDispatcher)

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
    @Provides
    @Sha1
    fun provideSha1Hasher(): Hasher = CryptographyProvider.Default.get(SHA1).hasher()

    @SingleIn(AppScope::class)
    @Provides
    @Sha256
    fun provideSha256Hasher(): Hasher = CryptographyProvider.Default.get(SHA256).hasher()

    @SingleIn(AppScope::class)
    @Provides
    fun provideJsonFileListStoreFactory(
        json: Json,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        coroutineScope: CoroutineScope,
        watcher: KfsDirectoryWatcher
    ): JsonFileListStoreFactory = object : JsonFileListStoreFactory {
        override fun <T> invoke(name: String, path: Path, serializer: KSerializer<T>): JsonFileListStore<T> =
            JsonFileListStoreImpl(name, path, serializer, json, ioDispatcher, coroutineScope, watcher)
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideJsonFileStoreFactory(
        json: Json,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        coroutineScope: CoroutineScope,
        watcher: KfsDirectoryWatcher
    ): JsonFileStoreFactory = object : JsonFileStoreFactory {
        override fun <T> invoke(
            name: String,
            path: Path,
            serializer: KSerializer<T>,
            defaultProvider: () -> T
        ): JsonFileStore<T> =
            JsonFileStoreImpl(name, path, serializer, defaultProvider, json, ioDispatcher, coroutineScope, watcher)
    }

    @SingleIn(AppScope::class)
    @Named("metaBaseUrl")
    @Provides
    fun provideMetaBaseUrl(): String = META_BASE_URL
}

@Qualifier
annotation class IODispatcher

@Qualifier
annotation class DefaultDispatcher

@Qualifier
annotation class MainDispatcher

@Qualifier
annotation class Sha1

@Qualifier
annotation class Sha256

internal expect val platformHttpClientEngineFactory: HttpClientEngineFactory<*>
