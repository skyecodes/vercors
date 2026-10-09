package app.vercors.core.system

data class ProcessResult(val exitCode: Int, val stdout: String, val stderr: String)

interface ProcessRunner {
    suspend fun run(command: List<String>, workingDir: String): ProcessResult
}
