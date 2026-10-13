package app.vercors.shared.data

import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.io.Buffer
import kotlinx.io.files.Path

// kzip buffers every compressed entry in memory until close and has no zip64, unlike the JVM's streaming writer
actual class ZipWriter actual constructor(path: Path) : AutoCloseable {
    private val zip = Zip.open(path, mode = Zip.Mode.Write)

    actual fun addFile(entryName: String, source: Path) {
        zip.entryFromPath(entryName.toZipEntryPath(), source)
    }

    actual fun addBytes(entryName: String, bytes: ByteArray) {
        zip.entryFromSource(entryName.toZipEntryPath(), Buffer().apply { write(bytes) })
    }

    actual override fun close() {
        zip.close()
    }
}

private fun String.toZipEntryPath(): Path {
    val segments = split('/').filter { it.isNotEmpty() }
    return Path(segments.first(), *segments.drop(1).toTypedArray())
}
