package app.vercors.launcher.cli

import io.github.oshai.kotlinlogging.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.fflush
import platform.posix.fopen
import platform.posix.fputs
import kotlin.time.Clock

actual fun configureLoggingPre() {
    KotlinLoggingConfiguration.logStartupMessage = false
    KotlinLoggingConfiguration.loggerFactory = DirectLoggerFactory
    KotlinLoggingConfiguration.direct.logLevel = Level.DEBUG
    KotlinLoggingConfiguration.direct.appender = NoOpAppender
}

@OptIn(ExperimentalForeignApi::class)
actual fun configureLoggingPost(logDir: Path) {
    SystemFileSystem.createDirectories(logDir)
    val file = fopen(Path(logDir, "launcher-cli.log").toString(), "a") ?: return
    KotlinLoggingConfiguration.direct.appender = object : Appender {
        // stdio locks the stream per call, so concurrent loggers don't interleave lines
        override fun log(loggingEvent: KLoggingEvent) {
            fputs(loggingEvent.format(), file)
            fflush(file)
        }
    }
}

private object NoOpAppender : Appender {
    override fun log(loggingEvent: KLoggingEvent) = Unit
}

private fun KLoggingEvent.format(): String = buildString {
    append("${Clock.System.now()} ${level.name.padEnd(5)} $loggerName - $message\n")
    cause?.let { append(it.stackTraceToString()).append('\n') }
}
