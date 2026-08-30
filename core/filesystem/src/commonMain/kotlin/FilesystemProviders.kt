package app.vercors.core.filesystem

import app.vercors.core.domain.AppConstants
import ca.gosyer.appdirs.AppDirs
import dev.zacsweers.metro.*
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
interface FilesystemProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideAppDirs(@Named(AppConstants.APP_ID_KEY) appId: String): AppDirs = AppDirs {
        appName = appId
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideAppPath(appDirs: AppDirs): Path = Path(appDirs.getUserDataDir())

    @Provides
    fun provideDirectoryWatcher(coroutineScope: CoroutineScope): KfsDirectoryWatcher = KfsDirectoryWatcher(
        scope = coroutineScope,
        dispatcher = Dispatchers.IO
    )

    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}
