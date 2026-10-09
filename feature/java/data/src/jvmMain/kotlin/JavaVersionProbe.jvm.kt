package app.vercors.feature.java.data

import app.vercors.feature.java.domain.model.JavaProbeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

internal actual suspend fun probeJavaExecutable(path: String): JavaProbeResult? = withContext(Dispatchers.IO) {
    try {
        val process = ProcessBuilder(path, "-XshowSettings:properties", "-version")
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val finished = process.waitFor(5, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            return@withContext null
        }
        parseJavaProbeOutput(output)
    } catch (_: Exception) {
        null
    }
}
