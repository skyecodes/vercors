package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import app.vercors.core.ui.state.produceAndCollectAsProgressiveState
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.map

@Composable
internal fun observeInstanceView(
    observeInstance: ObserveInstanceUseCase,
    instanceId: InstanceId,
): State<ProgressiveOutcome<InstanceError, InstanceView>> =
    produceAndCollectAsProgressiveState(instanceId) {
        observeInstance(instanceId).map { outcome -> outcome.map { it.toView() } }
    }
