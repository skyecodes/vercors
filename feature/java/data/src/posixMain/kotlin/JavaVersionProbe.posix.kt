package app.vercors.feature.java.data

import app.vercors.feature.java.domain.model.JavaProbeResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun probeJavaExecutable(path: String): JavaProbeResult? = withContext(Dispatchers.IO) {
    try {
        val command = "${path.shellQuoted()} -XshowSettings:properties -version 2>&1"
        val process = popen(command, "r") ?: return@withContext null
        val output = StringBuilder()
        val buffer = ByteArray(4096)
        while (true) {
            val line = fgets(buffer.refTo(0), buffer.size, process) ?: break
            output.append(line.toKString())
        }
        pclose(process)
        parseJavaProbeOutput(output.toString())
    } catch (_: Exception) {
        null
    }
}

private fun String.shellQuoted(): String = "'" + replace("'", "'\\''") + "'"
