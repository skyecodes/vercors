package app.vercors.feature.instance.domain.model

import kotlin.time.Clock
import kotlin.time.Instant

sealed interface InstanceLogEvent {
    val inst: Instant

    sealed class Plain(private val text: String) : InstanceLogEvent {
        final override val inst: Instant = Clock.System.now()
        override fun toString() = text
    }

    object Empty : Plain("")

    class Launcher(text: String) : Plain(text)

    class Error(text: String) : Plain(text)

    class GamePlain(text: String) : Plain(text)

    data class GameLog4j(
        private val logger: String,
        private val timestamp: Long,
        val level: String,
        private val thread: String,
        private val message: String
    ) : InstanceLogEvent {
        override val inst: Instant = Instant.fromEpochMilliseconds(timestamp)
        override fun toString(): String = message
    }
}
