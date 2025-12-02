package app.vercors.launcher.gui.instance.list

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.composeunstyled.Text
import pro.respawn.flowmvi.api.IntentReceiver
import pro.respawn.flowmvi.compose.dsl.subscribe

@Composable
fun InstanceListScreen(
    container: InstanceListContainer,
    navigateToInstanceDetailsAction: (String) -> Unit
) = with(container.store) {
    val state by subscribe { action ->
        when (action) {
            is InstanceListAction.NavigateToInstanceDetails -> navigateToInstanceDetailsAction(action.instanceId)
        }
    }
    InstanceListScreen(state)
}

@Composable
private fun IntentReceiver<InstanceListIntent>.InstanceListScreen(state: InstanceListState) {
    when (state) {
        is InstanceListState.Error -> Text("Error: ${state.throwable.message}")
        is InstanceListState.Loaded -> Column {
            if (state.instances.isEmpty())
                Text("No instances found.")
            else
                state.instances.forEach { instance ->
                    Text(instance.name)
                }
        }

        InstanceListState.Loading -> Text("Loading...")
    }
}