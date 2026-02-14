package app.vercors.shared.ui

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SnackbarServiceImpl : SnackbarService {
    override val messages: ReceiveChannel<SnackbarMessage>
        field = Channel<SnackbarMessage>()

    override suspend fun emit(message: SnackbarMessage) {
        messages.send(message)
    }
}
