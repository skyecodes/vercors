package app.vercors.feature.config.ui.storage

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.ui.datadirectory.ChangeDataDirectoryDialog

@Immutable
data class StorageConfigurationState(
    val dataDirectory: String,
    val canChangeDataDirectory: Boolean,
    val promptChangeDataDirectory: Boolean,
    val changeDataDirectoryDialog: ChangeDataDirectoryDialog,
    val contentCacheSizeBytes: Long?,
    val promptClearContentCache: Boolean,
)
