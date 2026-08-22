package app.vercors.shared.ui

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

private val logger = KotlinLogging.logger {}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SnackbarServiceImpl : SnackbarService {
    override val messages: ReceiveChannel<SnackbarMessage>
        field = Channel<SnackbarMessage>()

    override suspend fun emit(message: SnackbarMessage) {
        when (message.type) {
            SnackbarMessage.Type.INFO -> logger.info { "Showing info snackbar: ${message.message}" }
            SnackbarMessage.Type.SUCCESS -> logger.info { "Showing success snackbar: ${message.message}" }
            SnackbarMessage.Type.ERROR -> logger.error { "Showing error snackbar: ${message.message}" }
            SnackbarMessage.Type.WARN -> logger.warn { "Showing warn snackbar: ${message.message}" }
        }
        messages.send(message)
    }
}
