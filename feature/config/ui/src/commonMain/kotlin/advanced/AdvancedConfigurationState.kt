package app.vercors.feature.config.ui.advanced

import androidx.compose.runtime.Immutable

@Immutable
data class AdvancedConfigurationState(
    val metaApiUrl: String?,
    val networkTimeoutSeconds: Int?,
    val parallelDownloads: Int?,
    val parallelFilesystemOperations: Int?,
)
