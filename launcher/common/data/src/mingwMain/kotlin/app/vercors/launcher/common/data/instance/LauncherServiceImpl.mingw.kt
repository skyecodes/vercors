package app.vercors.launcher.common.data.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.io.files.Path

internal actual val platformPathSeparator: String = ";"
internal actual suspend fun runCommand(
    command: List<String>,
    workingDir: Path,
    dispatcher: CoroutineDispatcher
) {
    // TODO implement
}
