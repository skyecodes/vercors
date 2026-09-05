package app.vercors.feature.launcher.data.service

import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.launcher.data.dto.Log4jEvent
import app.vercors.feature.launcher.domain.model.InstanceLaunchEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.io.files.Path
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

internal actual val platformPathSeparator: String = File.pathSeparator

internal actual suspend fun ProducerScope<InstanceLaunchEvent>.runCommand(
    command: List<String>,
    workingDir: Path,
    loggingType: GameVersionDetails.Logging.Type?,
    context: CoroutineContext
) = withContext(context) {
    val processBuilder = ProcessBuilder(command).directory(File(workingDir.toString()))
    sendLog(InstanceLogEvent.Empty)
    logger.trace { "Running command: $command" }
    System.gc()
    val process = processBuilder.start()
    this@runCommand.setupErrorParser(process)
    when (loggingType) {
        GameVersionDetails.Logging.Type.LOG4J2_XML -> this@runCommand.setupLog4jParser(process)
        null -> this@runCommand.setupSimpleLogParser(process)
    }
    try {
        while (process.isAlive) {
            delay(100.milliseconds)
        }
    } catch (e: CancellationException) {
        process.destroy()
        withContext(NonCancellable) {
            sendLog(InstanceLogEvent.Empty)
            sendLog(InstanceLogEvent.Launcher("Stopping instance..."))
            send(InstanceLaunchEvent.Stopping {
                withContext(context) {
                    process.destroyForcibly()
                    logger.warn { "Instance run killed" }
                    sendLog(InstanceLogEvent.Launcher("Instance run killed"))
                }
            })
            process.waitFor()
            logger.info { "Instance run stopped" }
            sendLog(InstanceLogEvent.Launcher("Instance run stopped"))
        }
        throw e
    } finally {
        logger.info { "Instance run finished with code ${process.exitValue()}" }
        withContext(NonCancellable) {
            if (process.exitValue() == 0) sendLog(InstanceLogEvent.Empty)
            sendLog(InstanceLogEvent.Launcher("Instance run finished with code ${process.exitValue()}"))
            close()
        }
    }
}

private suspend fun SendChannel<InstanceLaunchEvent>.sendLog(logEvent: InstanceLogEvent) =
    send(InstanceLaunchEvent.Logging(logEvent))

private fun ProducerScope<InstanceLaunchEvent>.setupErrorParser(
    process: Process,
    context: CoroutineContext = Dispatchers.IO
) = launch(context) {
    process.errorReader().use {
        var line = it.readLine()?.replace("\t", "")
        while (line != null && process.isAlive) {
            this@setupErrorParser.sendLog(InstanceLogEvent.Error(line))
            line = it.readLine()?.replace("\t", "")
        }
    }
}


private fun ProducerScope<InstanceLaunchEvent>.setupLog4jParser(
    process: Process,
    context: CoroutineContext = Dispatchers.IO
) = launch(context) {
    val xml = XML.v1 {
        policy {
            ignoreUnknownChildren()
        }
    }
    process.inputReader().use {
        var line = it.readLine()?.replace("log4j:", "")
        val currentStack = mutableListOf<String>()
        while (line != null && process.isAlive) {
            this@setupLog4jParser.processLog4jLog(currentStack, line, xml)
            line = it.readLine()?.replace("log4j:", "")
        }
    }
}

private suspend fun ProducerScope<InstanceLaunchEvent>.processLog4jLog(
    currentStack: MutableList<String>,
    line: String,
    xml: XML
) {
    currentStack += line
    if (line.endsWith("</Event>")) {
        try {
            val fullLog = currentStack.joinToString("\n").trim()
            val splitIndex = fullLog.indexOf("<Event")
            if (splitIndex > 0) sendLog(InstanceLogEvent.GamePlain(fullLog.substring(0, splitIndex)))
            val logEvent: Log4jEvent = xml.decodeFromString(fullLog.substring(splitIndex))
            val message = logEvent.throwable?.let { "${logEvent.message}: $it" } ?: logEvent.message
            sendLog(
                InstanceLogEvent.GameLog4j(
                    logEvent.logger,
                    logEvent.timestamp,
                    logEvent.level,
                    logEvent.thread,
                    message
                )
            )
        } catch (e: Exception) {
            logger.warn(e) { "Unable to parse Log4j log event" }
        }
        currentStack.clear()
    }
}

private fun ProducerScope<InstanceLaunchEvent>.setupSimpleLogParser(
    process: Process,
    context: CoroutineContext = Dispatchers.IO
) = launch(context) {
    process.inputReader().use {
        var line = it.readLine()
        while (line != null && process.isAlive) {
            this@setupSimpleLogParser.sendLog(InstanceLogEvent.GamePlain(line))
            line = it.readLine()
        }
    }
}
