package app.vercors.core.system

expect class ProcessRunnerImpl : ProcessRunner {
    override suspend fun run(command: List<String>, workingDir: String): ProcessResult
}
