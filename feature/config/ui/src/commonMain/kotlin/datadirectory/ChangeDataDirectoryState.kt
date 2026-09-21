package app.vercors.feature.config.ui.datadirectory

import androidx.compose.runtime.Immutable

@Immutable
data class ChangeDataDirectoryState(
    val pathText: String,
    val isMoving: Boolean = false,
    val moveProgress: Float? = null,
    val errorMessage: String? = null,
) {
    val canConfirm: Boolean = !isMoving
}
