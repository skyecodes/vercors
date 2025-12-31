package app.vercors.feature.launcher.data.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.File

private val logger = KotlinLogging.logger {}

internal actual val platformPathSeparator: String = File.pathSeparator

internal actual suspend fun runCommand(
    command: List<String>,
    workingDir: Path,
    dispatcher: CoroutineDispatcher
) {
    val process = ProcessBuilder(command).directory(File(workingDir.toString()))
        .inheritIO() // TODO remove; for debugging purposes only
    withContext(dispatcher) {
        logger.info { "Running command: $command" }
        System.gc()
        process.start().waitFor()
        logger.info { "Command finished" }
    }
}
