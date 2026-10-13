package app.vercors.shared.data

import app.vercors.core.filesystem.delete
import app.vercors.core.filesystem.moveTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path

// Zip entry names always use '/', but Path.toString() uses the platform separator ('\' on Windows)
fun Path.toZipEntryName(): String = parent?.let { "${it.toZipEntryName()}/$name" } ?: name

// Written to a sibling tmp file so a failed or cancelled export never leaves a truncated zip at the target
suspend fun Path.writeZipAtomically(block: suspend ZipWriter.() -> Unit) {
    val tmp = Path("$this.tmp")
    try {
        withContext(Dispatchers.IO) { ZipWriter(tmp).use { it.block() } }
        tmp.moveTo(this)
    } catch (e: Throwable) {
        withContext(NonCancellable) { tmp.delete(mustExist = false) }
        throw e
    }
}
