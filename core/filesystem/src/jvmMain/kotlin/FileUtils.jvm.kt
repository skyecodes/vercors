package app.vercors.core.filesystem

import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.createLinkPointingTo
import kotlin.io.path.createParentDirectories
import kotlin.io.path.createSymbolicLinkPointingTo
import kotlin.io.path.getLastModifiedTime
import kotlin.time.Instant
import kotlin.io.path.Path as JvmPath

actual suspend fun Path.createLink(target: Path, context: CoroutineContext, isSymbolic: Boolean): Boolean {
    val src = JvmPath(toString())
    val dst = JvmPath(target.toString())
    dst.createParentDirectories()
    withContext(context) {
        // The receiver is the new link, the argument the existing file
        if (isSymbolic) dst.createSymbolicLinkPointingTo(src) else dst.createLinkPointingTo(src)
    }
    return true
}

actual suspend fun Path.lastModifiedOrNull(context: CoroutineContext): Instant? {
    val jvmPath = JvmPath(toString())
    return withContext(context) {
        runCatching { jvmPath.getLastModifiedTime().toMillis() }
            .getOrNull()
            ?.let { Instant.fromEpochMilliseconds(it) }
    }
}

actual suspend fun Path.setExecutable(context: CoroutineContext): Boolean {
    val jvmPath = JvmPath(toString())
    return withContext(context) {
        jvmPath.toFile().setExecutable(true)
    }
}

actual suspend fun Path.restrictToOwner(context: CoroutineContext): Boolean {
    val jvmPath = JvmPath(toString())
    return withContext(context) {
        runCatching {
            if ("posix" in jvmPath.fileSystem.supportedFileAttributeViews()) {
                Files.setPosixFilePermissions(
                    jvmPath, setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                )
            } else {
                val file = jvmPath.toFile()
                var result = file.setReadable(false, false)
                result = result && file.setWritable(false, false)
                result = result && file.setReadable(true, true)
                result = result && file.setWritable(true, true)
                if (!result) throw IOException("Failed to restrict permissions")
            }
        }.isSuccess
    }
}

actual suspend fun Path.realPath(context: CoroutineContext): Path? {
    val jvmPath = JvmPath(toString())
    return withContext(context) {
        runCatching { Path(jvmPath.toRealPath().toString()) }.getOrNull()
    }
}
