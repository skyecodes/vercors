package app.vercors.launcher.instance.presentation.details

import androidx.compose.runtime.Composable
import app.vercors.launcher.core.presentation.mvi.MviContainer
import app.vercors.launcher.instance.domain.InstanceId
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun InstanceDetailsScreen(instanceId: InstanceId) {
    MviContainer(koinViewModel<InstanceDetailsViewModel>(parameters = { parametersOf(instanceId) })) { uiState, onIntent ->
        InstanceDetailsScreen(
            state = uiState,
            onIntent = onIntent
        )
    }
}

@Composable
fun InstanceDetailsScreen(
    state: InstanceDetailsUiState,
    onIntent: (InstanceDetailsIntent) -> Unit
) {
    // TODO
}