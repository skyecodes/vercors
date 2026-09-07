package app.vercors.feature.java.data.service

import app.vercors.feature.java.domain.model.JavaProbeResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.posix._pclose
import platform.posix._popen

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun probeJavaExecutable(path: String): JavaProbeResult? = withContext(Dispatchers.IO) {
    try {
        val command = "${path.shellQuoted()} -XshowSettings:properties -version 2>&1"
        val process = _popen(command, "r") ?: return@withContext null
        val output = StringBuilder()
        val buffer = ByteArray(4096)
        while (true) {
            val line = platform.posix.fgets(buffer.refTo(0), buffer.size, process) ?: break
            output.append(line.toKString())
        }
        _pclose(process)
        parseJavaProbeOutput(output.toString())
    } catch (_: Exception) {
        null
    }
}

private fun String.shellQuoted(): String = "\"" + replace("\"", "\\\"") + "\""
