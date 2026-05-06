package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Immutable
import app.vercors.feature.game.ui.displayName
import app.vercors.feature.instance.domain.model.*
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.time.Instant

@Immutable
data class InstanceDetailsState(
    val instance: ProgressiveOutcome<InstanceError, InstanceView>,
    val promptDeleteConfirmation: Boolean,
    val logs: List<InstanceLogEventView>?,
)

@Immutable
data class InstanceView(
    val id: InstanceId,
    val name: String,
    val version: String,
    val loader: String,
    val type: String,
    val status: InstanceStatus,
)

@Immutable
data class InstanceLogEventView(
    val timestamp: Instant,
    val level: Level,
    val message: String,
    val isLauncher: Boolean,
) {
    enum class Level {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }
}

fun InstanceAndStatus.toView(): InstanceView = InstanceView(
    id = instance.id,
    name = instance.name,
    version = instance.gameVersion,
    loader = instance.modLoader?.type.displayName,
    type = instance.displayType,
    status = status
)

val Instance.displayType: String get() = if (modpackInfo != null) "Modpack" else "Custom"

fun InstanceLogEvent.toView(): InstanceLogEventView = when (this) {
    is InstanceLogEvent.GameLog4j -> InstanceLogEventView(
        timestamp = inst,
        level = InstanceLogEventView.Level.valueOf(level.uppercase()),
        message = toString(),
        isLauncher = false
    )

    is InstanceLogEvent.Plain -> InstanceLogEventView(
        timestamp = inst,
        level = if (this is InstanceLogEvent.Error) InstanceLogEventView.Level.ERROR else InstanceLogEventView.Level.INFO,
        message = toString(),
        isLauncher = this is InstanceLogEvent.Launcher
    )
}
