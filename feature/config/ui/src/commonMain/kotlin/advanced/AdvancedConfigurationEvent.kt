package app.vercors.feature.config.ui.advanced

import app.vercors.shared.ui.Event

sealed interface AdvancedConfigurationEvent : Event {
    data class OnMetaApiUrlChange(val value: String) : AdvancedConfigurationEvent
    data class OnNetworkTimeoutChange(val value: Int) : AdvancedConfigurationEvent
    data class OnParallelDownloadsChange(val value: Int) : AdvancedConfigurationEvent
    data class OnParallelFilesystemOperationsChange(val value: Int) : AdvancedConfigurationEvent
}
