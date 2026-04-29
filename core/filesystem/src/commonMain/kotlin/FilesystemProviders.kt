package app.vercors.core.filesystem

import ca.gosyer.appdirs.AppDirs
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
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
    fun provideAppDirs(): AppDirs = AppDirs {
        appName = "vercors-launcher"
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
