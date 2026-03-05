package app.vercors.feature.launcher.domain.model

import app.vercors.feature.instance.domain.model.InstanceLogEvent

sealed interface InstanceLaunchEvent {
    data class Validating(val progress: Float?) : InstanceLaunchEvent
    data class Downloading(val progress: Float?) : InstanceLaunchEvent
    data class Running(val stop: suspend () -> Unit) : InstanceLaunchEvent
    data class Stopping(val kill: suspend () -> Unit) : InstanceLaunchEvent
    data class Logging(val logEvent: InstanceLogEvent) : InstanceLaunchEvent
}
