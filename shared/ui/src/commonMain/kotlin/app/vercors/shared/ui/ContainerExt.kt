package app.vercors.shared.ui

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KMarkerFactory
import pro.respawn.flowmvi.api.FlowMVIDSL
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.dsl.StoreBuilder
import pro.respawn.flowmvi.logging.StoreLogLevel
import pro.respawn.flowmvi.logging.StoreLogger

@FlowMVIDSL
fun <S : MVIState, I : MVIIntent, A : MVIAction> StoreBuilder<S, I, A>.configureWithLogger(
    logger: KLogger,
    debuggable: Boolean = true,
): Unit =
    configure {
        this.debuggable = debuggable
        this.name = logger.name
        this.logger = StoreLogger { level: StoreLogLevel, tag: String?, message: () -> String ->
            val marker = tag?.let { KMarkerFactory.getMarker(it) }
            when (level) {
                StoreLogLevel.Trace -> logger.trace(throwable = null, marker = marker) { message() }
                StoreLogLevel.Debug -> logger.debug(throwable = null, marker = marker) { message() }
                StoreLogLevel.Info -> logger.info(throwable = null, marker = marker) { message() }
                StoreLogLevel.Warn -> logger.warn(throwable = null, marker = marker) { message() }
                StoreLogLevel.Error -> logger.error(throwable = null, marker = marker) { message() }
            }
        }
    }
