package app.vercors.core.system

data class ProcessResult(val exitCode: Int, val stdout: String, val stderr: String)

/**
 * Runs a command to completion and captures its output.
 *
 * For short-lived, one-shot commands (e.g. a mod loader installer's processors). Not meant for long-running
 * processes that need to stream output as they run — that's what the launcher's own `runCommand` is for.
 */
interface ProcessRunner {
    suspend fun run(command: List<String>, workingDir: String): ProcessResult
}
