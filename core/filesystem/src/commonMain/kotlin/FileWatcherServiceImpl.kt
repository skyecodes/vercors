package app.vercors.core.filesystem

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.irgaly.kfswatch.KfsDirectoryWatcher
import io.github.irgaly.kfswatch.KfsDirectoryWatcherEvent
import io.github.irgaly.kfswatch.KfsEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FileWatcherServiceImpl(
    private val watcher: KfsDirectoryWatcher
) : FileWatcherService {
    private val watchMap = mutableMapOf<String, Int>()
    private val mutex = Mutex()

    override fun watchFile(filePath: Path): Flow<FileEvent> {
        val directory = (filePath.parent ?: Path(".")).toString()
        return watcher.onEventFlow
            .filter { it.targetDirectory == directory && it.path == filePath.name }
            .watch(directory)
    }

    override fun watchDirectory(directoryPath: Path): Flow<FileEvent> {
        val directory = directoryPath.toString()
        return watcher.onEventFlow
            .filter { it.targetDirectory == directory }
            .watch(directory)
    }

    private fun Flow<KfsDirectoryWatcherEvent>.watch(directory: String) = map { it.convert() }
        .onStart {
            val count = mutex.withLock {
                val current = watchMap.getOrElse(directory) { 0 }
                watchMap[directory] = current + 1
                current
            }
            if (count == 0) {
                logger.debug { "Starting to watch directory $directory" }
                watcher.add(directory)
            }
        }
        .onEach { logger.trace { "File event received: $it" } }
        .onCompletion {
            val isLast = mutex.withLock {
                val current = watchMap[directory] ?: return@withLock false
                val newCount = current - 1
                if (newCount <= 0) {
                    watchMap.remove(directory)
                    true
                } else {
                    watchMap[directory] = newCount
                    false
                }
            }
            if (isLast) {
                logger.debug { "Stopping to watch directory $directory" }
                watcher.remove(directory)
            }
        }

    private fun KfsDirectoryWatcherEvent.convert(): FileEvent = FileEvent(
        path = Path(targetDirectory, path),
        type = when (event) {
            KfsEvent.Create -> FileEvent.Type.CREATE
            KfsEvent.Delete -> FileEvent.Type.DELETE
            KfsEvent.Modify -> FileEvent.Type.MODIFY
        }
    )
}
