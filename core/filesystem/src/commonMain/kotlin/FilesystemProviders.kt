package app.vercors.core.filesystem

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json

@ContributesTo(AppScope::class)
interface FilesystemProviders {
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
