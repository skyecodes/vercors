package app.vercors.launcher.gui.instance.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun CreateInstanceScreen(
    container: CreateInstanceContainer,
) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            else -> {}
        }
    }
    CreateInstanceScreen(state)
}

@Composable
context(store: IntentReceiver<CreateInstanceIntent>)
private fun CreateInstanceScreen(state: CreateInstanceState) {
}
