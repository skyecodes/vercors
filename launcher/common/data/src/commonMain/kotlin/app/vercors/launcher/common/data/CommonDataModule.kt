package app.vercors.launcher.common.data

import ca.gosyer.appdirs.AppDirs
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.koin.core.annotation.*

@Module
@ComponentScan
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