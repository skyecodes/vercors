package app.vercors.core.filesystem

import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
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
        if (isSymbolic) src.createSymbolicLinkPointingTo(dst) else src.createLinkPointingTo(dst)
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

actual suspend fun Path.realPath(context: CoroutineContext): Path? {
    val jvmPath = JvmPath(toString())
    return withContext(context) {
        runCatching { Path(jvmPath.toRealPath().toString()) }.getOrNull()
    }
}
