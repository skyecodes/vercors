package app.vercors.feature.instance.ui.details.logs

import androidx.compose.runtime.Immutable
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import kotlin.time.Instant

@Immutable
data class InstanceLogsState(
    val logs: List<InstanceLogEventView>?,
    val searchQuery: String = "",
    val levelFilter: Set<InstanceLogEventView.Level> = InstanceLogEventView.Level.entries.toSet(),
    val wrapLines: Boolean = true,
) {
    val filteredLogs = logs?.filter {
        it.level in levelFilter && (searchQuery.isBlank() || it.message.contains(searchQuery, ignoreCase = true))
    }
}

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
