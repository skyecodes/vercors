package app.vercors.feature.config.ui.nativelibraries

import app.vercors.shared.ui.Event

sealed interface NativeLibraryConfigurationEvent : Event {
    data class OnUseSystemGlfwToggle(val checked: Boolean) : NativeLibraryConfigurationEvent
    data class OnGlfwPathChange(val value: String) : NativeLibraryConfigurationEvent
    data class OnUseSystemOpenAlToggle(val checked: Boolean) : NativeLibraryConfigurationEvent
    data class OnOpenAlPathChange(val value: String) : NativeLibraryConfigurationEvent
}
