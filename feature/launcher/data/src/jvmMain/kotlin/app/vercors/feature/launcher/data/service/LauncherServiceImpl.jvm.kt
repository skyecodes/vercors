package app.vercors.feature.launcher.data.service

import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.launcher.data.dto.Log4jEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger {}

internal actual val platformPathSeparator: String = File.pathSeparator

internal actual fun runCommand(
    command: List<String>,
    workingDir: Path,
    loggingType: GameVersionDetails.Logging.Type?,
    context: CoroutineContext
): Flow<InstanceLogEvent> = channelFlow {
    val processBuilder = ProcessBuilder(command).directory(File(workingDir.toString()))
    //.inheritIO()
    logger.info { "Starting instance run at $workingDir" }
    send(InstanceLogEvent.Launcher("Starting instance run at $workingDir"))
    send(InstanceLogEvent.Empty)
    logger.trace { "Running command: $command" }
    System.gc()
    val process = processBuilder.start()
    setupErrorParser(process)
    when (loggingType) {
        GameVersionDetails.Logging.Type.LOG4J2_XML -> setupLog4jParser(process)
        null -> setupSimpleLogParser(process)
    }
    try {
        while (process.isAlive) {
            delay(100)
        }
    } catch (e: CancellationException) {
        process.destroy()
        logger.info { "Instance run cancelled" }
        send(InstanceLogEvent.Empty)
        send(InstanceLogEvent.Launcher("Instance run cancelled"))
        throw e
    }
    logger.info { "Instance run finished with code ${process.exitValue()}" }
    send(InstanceLogEvent.Empty)
    send(InstanceLogEvent.Launcher("Instance run finished with code ${process.exitValue()}"))
}.flowOn(context)

context(flow: ProducerScope<InstanceLogEvent>)
private fun setupErrorParser(process: Process, context: CoroutineContext = Dispatchers.IO) = flow.launch(context) {
    process.errorReader().use {
        var line = it.readLine()?.replace("\t", "")
        while (line != null && process.isAlive) {
            flow.send(InstanceLogEvent.Error(line))
            line = it.readLine()?.replace("\t", "")
        }
    }
}


context(flow: ProducerScope<InstanceLogEvent>)
private fun setupLog4jParser(process: Process, context: CoroutineContext = Dispatchers.IO) =
    flow.launch(context) {
        val xml = XML.v1 {
            policy {
                ignoreUnknownChildren()
            }
        }
        process.inputReader().use {
            var line = it.readLine()?.replace("log4j:", "")
            val currentStack = mutableListOf<String>()
            while (line != null && process.isAlive) {
                processLog4jLog(currentStack, line, xml)
                line = it.readLine()?.replace("log4j:", "")
            }
        }
    }

context(flow: ProducerScope<InstanceLogEvent>)
private suspend fun processLog4jLog(currentStack: MutableList<String>, line: String, xml: XML) {
    currentStack += line.replace("\t", "\n")
    if (line.endsWith("</Event>")) {
        try {
            val fullLog = currentStack.joinToString("").trim()
            val splitIndex = fullLog.indexOf("<Event")
            if (splitIndex > 0) flow.send(InstanceLogEvent.GamePlain(fullLog.substring(0, splitIndex)))
            val logEvent: Log4jEvent = xml.decodeFromString(fullLog.substring(splitIndex))
            val message = logEvent.throwable?.let { "${logEvent.message}: $it" } ?: logEvent.message
            flow.send(
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

context(flow: ProducerScope<InstanceLogEvent>)
private fun setupSimpleLogParser(process: Process, context: CoroutineContext = Dispatchers.IO) =
    flow.launch(context) {
        process.inputReader().use {
            var line = it.readLine()
            while (line != null && process.isAlive) {
                flow.send(InstanceLogEvent.GamePlain(line))
                line = it.readLine()
            }
        }
    }
