package app.vercors.feature.launcher.data.service

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.io.files.Path

internal actual val platformPathSeparator: String = ":"
internal actual suspend fun runCommand(
    command: List<String>,
    workingDir: Path,
    dispatcher: CoroutineDispatcher
) {
    // TODO implement
}
