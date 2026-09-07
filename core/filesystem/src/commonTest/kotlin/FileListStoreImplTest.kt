package app.vercors.core.filesystem

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

class FileListStoreImplTest {
    @Test
    fun `store creates its directory before registering the file watcher on it`() = runTest {
        val dir = Path("build/file-list-store-test/watch-dir-race")
        if (SystemFileSystem.exists(dir)) dir.deleteRecursively()

        val directoryExistedWhenWatchStarted = CompletableDeferred<Boolean>()
        val fileWatcherService = object : FileWatcherService {
            override fun watchFile(filePath: Path): Flow<FileEvent> = emptyFlow()
            override fun watchDirectory(directoryPath: Path): Flow<FileEvent> {
                directoryExistedWhenWatchStarted.complete(SystemFileSystem.exists(directoryPath))
                return emptyFlow()
            }
        }

        val store = FileListStoreImpl(
            directoryPath = dir,
            serializer = String.serializer(),
            json = Json,
            fileWatcherService = fileWatcherService,
            backgroundScope = this,
            parallelFilesystemOperationsProvider = { 1 }
        )

        store.flow.first()

        assertTrue(directoryExistedWhenWatchStarted.await())
    }
}
