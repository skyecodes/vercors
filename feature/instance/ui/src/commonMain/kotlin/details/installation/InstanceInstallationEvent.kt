package app.vercors.feature.instance.ui.details.installation

import app.vercors.shared.ui.Event

sealed interface InstanceInstallationEvent : Event {
    data object OnFinishInstallationClick : InstanceInstallationEvent
    data object OnFinishInstallationDismiss : InstanceInstallationEvent
    data object OnResumeInstallationClick : InstanceInstallationEvent
}
