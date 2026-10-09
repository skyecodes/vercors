package app.vercors.feature.instance.ui.details.logs

import app.vercors.shared.ui.Event

sealed interface InstanceLogsEvent : Event {
    data class OnSearchQueryChange(val query: String) : InstanceLogsEvent
    data class OnLevelFilterToggle(val level: InstanceLogEventView.Level) : InstanceLogsEvent
    data object OnWrapLinesToggle : InstanceLogsEvent
}
