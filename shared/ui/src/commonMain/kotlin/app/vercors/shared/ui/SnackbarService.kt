package app.vercors.shared.ui

import androidx.compose.runtime.Composable
import kotlinx.coroutines.channels.ReceiveChannel

interface SnackbarService {
    val messages: ReceiveChannel<SnackbarMessage>
    suspend fun emit(message: SnackbarMessage)
}

suspend fun SnackbarService.info(
    message: @Composable () -> String
) = emit(SnackbarMessage(SnackbarMessage.Type.INFO, message))

suspend fun SnackbarService.success(
    message: @Composable () -> String
) = emit(SnackbarMessage(SnackbarMessage.Type.SUCCESS, message))

suspend fun SnackbarService.warn(
    message: @Composable () -> String
) = emit(SnackbarMessage(SnackbarMessage.Type.WARN, message))

suspend fun SnackbarService.error(
    message: @Composable () -> String
) = emit(SnackbarMessage(SnackbarMessage.Type.ERROR, message))

data class SnackbarMessage(
    val type: Type,
    val message: @Composable () -> String,
    val onClick: (() -> Unit)? = null
) {
    enum class Type {
        INFO, SUCCESS, WARN, ERROR
    }
}
