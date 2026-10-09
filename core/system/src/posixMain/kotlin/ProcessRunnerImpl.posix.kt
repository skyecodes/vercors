package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

private const val BUFFER_SIZE = 4096
private const val EXIT_CODE_MARKER = "__VERCORS_EXIT_CODE__:"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ProcessRunnerImpl : ProcessRunner {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun run(command: List<String>, workingDir: String): ProcessResult {
        val shellCommand = "cd ${workingDir.shellQuote()} && " +
                command.joinToString(" ") { it.shellQuote() } +
                " 2>&1; echo \"$EXIT_CODE_MARKER$?\""
        val output = memScoped {
            val buffer = allocArray<ByteVar>(BUFFER_SIZE)
            val pipe = popen(shellCommand, "r") ?: error("Failed to start process: $command")
            val out = StringBuilder()
            while (fgets(buffer, BUFFER_SIZE, pipe) != null) {
                out.append(buffer.toKString())
            }
            pclose(pipe)
            out.toString()
        }
        val markerIndex = output.lastIndexOf(EXIT_CODE_MARKER)
        check(markerIndex >= 0) { "Process output did not contain an exit code: $command" }
        val exitCode = output.substring(markerIndex + EXIT_CODE_MARKER.length).trim().toInt()
        // popen exposes a single merged stream, reported as stdout
        return ProcessResult(exitCode, stdout = output.substring(0, markerIndex), stderr = "")
    }
}

private fun String.shellQuote(): String = "'" + replace("'", "'\\''") + "'"
