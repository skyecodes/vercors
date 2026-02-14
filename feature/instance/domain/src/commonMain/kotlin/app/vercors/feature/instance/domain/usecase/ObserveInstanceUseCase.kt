package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceAndStatus
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.combine
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.arrow.progressive
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    @OptIn(ExperimentalProgressiveRaiseApi::class)
    operator fun invoke(instanceId: InstanceId): ProgressiveFlow<InstanceError, InstanceAndStatus> =
        instanceRepository.instances.combine(instanceRepository.statuses) { _, statuses ->
            progressive {
                InstanceAndStatus(
                    instance = instanceRepository.findInstance(instanceId),
                    status = statuses[instanceId] ?: InstanceStatus.Idle
                )
            }
        }
}
