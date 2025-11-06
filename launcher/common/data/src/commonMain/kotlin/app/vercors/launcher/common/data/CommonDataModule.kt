package app.vercors.launcher.common.data

import ca.gosyer.appdirs.AppDirs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

@Module
@ComponentScan
class CommonDataModule

@Qualifier
annotation class IODispatcher

@Single
@IODispatcher
fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

@Qualifier
annotation class MainDispatcher

@Single
@MainDispatcher
fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

@Qualifier
annotation class DefaultDispatcher

@Single
@DefaultDispatcher
fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

@Single
fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

@Single
fun provideAppDirs(): AppDirs = AppDirs {
    appName = "vercors-launcher"
}