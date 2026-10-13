package app.vercors.shared.data

import kotlinx.io.files.Path

expect class ZipWriter(path: Path) : AutoCloseable {
    fun addFile(entryName: String, source: Path)

    fun addBytes(entryName: String, bytes: ByteArray)

    override fun close()
}
