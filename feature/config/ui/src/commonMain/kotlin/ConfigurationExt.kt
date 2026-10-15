package app.vercors.feature.config.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.usecase.ObserveInstanceListUseCase
import opensavvy.state.progressive.valueOrNull

@Composable
internal fun ObserveInstanceListUseCase.collectHasRunningInstance(): Boolean {
    val instances by this().collectProgressiveAsState()
    return instances.valueOrNull?.any { it.getOrNull()?.status is InstanceStatus.Running } == true
}
