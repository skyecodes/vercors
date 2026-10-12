package app.vercors.core.system

actual class ProcessRunnerImpl : ProcessRunner {
    actual override suspend fun run(command: List<String>, workingDir: String): ProcessResult =
        ProcessResult(exitCode = -1, stdout = "", stderr = "Processes are unavailable in the browser")
}
