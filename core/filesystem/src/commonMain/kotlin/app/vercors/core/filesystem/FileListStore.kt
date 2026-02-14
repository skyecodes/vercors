package app.vercors.core.filesystem

import arrow.core.Either
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import opensavvy.state.coroutines.ProgressiveFlow

interface FileListStore<T> {
    val flow: ProgressiveFlow<Nothing, Map<String, Either<FileStoreError, T>>>

    suspend fun write(name: String, data: T)

    suspend fun delete(name: String)
}

interface FileListStoreFactory {
    operator fun <T> invoke(directoryPath: Path, serializer: KSerializer<T>): FileListStore<T>
}
