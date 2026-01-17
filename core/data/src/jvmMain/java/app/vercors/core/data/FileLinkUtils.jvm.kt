package app.vercors.core.data

import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.createLinkPointingTo
import kotlin.io.path.createParentDirectories
import kotlin.io.path.createSymbolicLinkPointingTo
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
