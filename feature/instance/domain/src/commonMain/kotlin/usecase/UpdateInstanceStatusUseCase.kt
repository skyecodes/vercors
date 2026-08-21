package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject

@Inject
class UpdateInstanceStatusUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(instanceId: InstanceId, status: InstanceStatus) =
        instanceRepository.updateInstanceStatus(instanceId, status)
}
