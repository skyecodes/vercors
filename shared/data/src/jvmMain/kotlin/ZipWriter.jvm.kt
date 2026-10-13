package app.vercors.shared.data

import kotlinx.io.files.Path
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

actual class ZipWriter private constructor(private val output: ZipOutputStream) : AutoCloseable by output {
    actual constructor(path: Path) : this(ZipOutputStream(BufferedOutputStream(FileOutputStream(path.toString()))))

    actual fun addFile(entryName: String, source: Path) {
        output.putNextEntry(ZipEntry(entryName))
        FileInputStream(source.toString()).use { it.copyTo(output) }
        output.closeEntry()
    }

    actual fun addBytes(entryName: String, bytes: ByteArray) {
        output.putNextEntry(ZipEntry(entryName))
        output.write(bytes)
        output.closeEntry()
    }
}
