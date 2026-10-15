package app.vercors.feature.config.ui.storage

import app.vercors.shared.ui.Event

sealed interface StorageConfigurationEvent : Event {
    data object OnClearContentCacheClick : StorageConfigurationEvent
    data class OnConfirmClearContentCache(val result: Boolean) : StorageConfigurationEvent
    data object OnOpenDataDirectoryClick : StorageConfigurationEvent
    data object OnChangeDataDirectoryClick : StorageConfigurationEvent
    data object OnChangeDataDirectoryDismiss : StorageConfigurationEvent
}
