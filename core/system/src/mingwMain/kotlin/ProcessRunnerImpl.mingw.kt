package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.cinterop.*
import platform.posix._pclose
import platform.posix._popen
import platform.posix.fgets

private const val BUFFER_SIZE = 4096
private const val EXIT_CODE_MARKER = "__VERCORS_EXIT_CODE__:"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ProcessRunnerImpl : ProcessRunner {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun run(command: List<String>, workingDir: String): ProcessResult {
        // /V:ON delays expansion so the error level reflects the command that just ran
        val shellCommand = "cmd /V:ON /C \"cd /d ${workingDir.cmdQuote()} && " +
                command.joinToString(" ") { it.cmdQuote() } +
                " 2>&1 & echo $EXIT_CODE_MARKER!errorlevel!\""
        val output = memScoped {
            val buffer = allocArray<ByteVar>(BUFFER_SIZE)
            val pipe = _popen(shellCommand, "r") ?: error("Failed to start process: $command")
            val out = StringBuilder()
            while (fgets(buffer, BUFFER_SIZE, pipe) != null) {
                out.append(buffer.toKString())
            }
            _pclose(pipe)
            out.toString()
        }
        val markerIndex = output.lastIndexOf(EXIT_CODE_MARKER)
        check(markerIndex >= 0) { "Process output did not contain an exit code: $command" }
        val exitCode = output.substring(markerIndex + EXIT_CODE_MARKER.length).trim().toInt()
        // _popen exposes a single merged stream, reported as stdout
        return ProcessResult(exitCode, stdout = output.substring(0, markerIndex), stderr = "")
    }
}

private fun String.cmdQuote(): String = "\"" + replace("\"", "\"\"") + "\""
