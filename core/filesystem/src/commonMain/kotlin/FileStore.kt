package app.vercors.core.filesystem

import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import opensavvy.state.coroutines.ProgressiveFlow

interface FileStore<T> {
    val flow: ProgressiveFlow<FileStoreError, T>

    suspend fun write(data: T)
}

interface FileStoreFactory {
    operator fun <T> invoke(
        filePath: Path,
        serializer: KSerializer<T>,
        defaultProvider: (() -> T)? = null
    ): FileStore<T>
}

sealed interface FileStoreError {
    data object Serialization : FileStoreError
    data object IO : FileStoreError
}
