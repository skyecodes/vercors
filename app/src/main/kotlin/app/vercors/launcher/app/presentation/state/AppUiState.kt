package app.vercors.launcher.app.presentation.state

import androidx.compose.runtime.Immutable

@Immutable
data class AppUiState(
    val undecorated: Boolean = true,
    val currentDialog: AppDialog? = null,
)