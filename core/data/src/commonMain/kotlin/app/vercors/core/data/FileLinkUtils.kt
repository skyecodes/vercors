package app.vercors.core.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.files.Path
import kotlin.coroutines.CoroutineContext

suspend fun Path.linkOrCopyTo(target: Path, context: CoroutineContext = Dispatchers.IO) =
    tryOrFalse { createLink(target, context) } ||
            tryOrFalse { createLink(target, context, true) } ||
            tryOrFalse { copyTo(target, context); true }

private suspend fun tryOrFalse(block: suspend () -> Boolean) = try {
    block()
} catch (_: Exception) {
    currentCoroutineContext().ensureActive()
    false
}

expect suspend fun Path.createLink(
    target: Path,
    context: CoroutineContext = Dispatchers.IO,
    isSymbolic: Boolean = false
): Boolean
