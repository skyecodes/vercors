package app.vercors.feature.config.ui.java

import app.vercors.shared.ui.Event

sealed interface JavaConfigurationEvent : Event {
    data class OnJavaInstallationSelected(val path: String) : JavaConfigurationEvent
    data object OnRescanJavaClick : JavaConfigurationEvent
    data class OnPreferredJavaChange(val majorVersion: Int, val installationId: String) : JavaConfigurationEvent
    data class OnForgetInstallation(val installationId: String) : JavaConfigurationEvent
    data object OnJavaAddErrorDismiss : JavaConfigurationEvent
    data class OnMinMemoryMbChange(val value: Int?) : JavaConfigurationEvent
    data class OnMaxMemoryMbChange(val value: Int?) : JavaConfigurationEvent
    data class OnExtraJvmArgsChange(val value: String) : JavaConfigurationEvent
}
