package app.vercors.core.filesystem

import kotlinx.coroutines.flow.Flow
import kotlinx.io.files.Path

interface FileWatcherService {
    fun watchFile(filePath: Path): Flow<FileEvent>
    fun watchDirectory(directoryPath: Path): Flow<FileEvent>
}

data class FileEvent(
    val path: Path,
    val type: Type
) {
    enum class Type {
        CREATE, MODIFY, DELETE
    }
}
