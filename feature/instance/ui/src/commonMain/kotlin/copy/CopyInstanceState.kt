package app.vercors.feature.instance.ui.copy

import androidx.compose.runtime.Immutable

@Immutable
data class CopyInstanceState(
    val instanceName: String,
    val isCopying: Boolean = false,
) {
    val canCopyInstance: Boolean = instanceName.length >= 3 && !isCopying
}
