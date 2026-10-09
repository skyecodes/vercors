package app.vercors.core.system

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
actual class ProcessRunnerImpl : ProcessRunner {
    actual override suspend fun run(command: List<String>, workingDir: String): ProcessResult =
        withContext(Dispatchers.IO) {
            val process = ProcessBuilder(command).directory(File(workingDir)).start()
            val stdout = async { process.inputStream.bufferedReader().readText() }
            val stderr = async { process.errorStream.bufferedReader().readText() }
            ProcessResult(exitCode = process.waitFor(), stdout = stdout.await(), stderr = stderr.await())
        }
}
