package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class UpdateInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    context(_: Raise<InstanceError>, _: Raise<InstanceValidationError>)
    suspend operator fun invoke(instanceId: InstanceId, update: (Instance) -> Instance) {
        val instance = instanceRepository.findInstance(instanceId)
        val newInstance = update(instance)
        if (instance != newInstance) {
            instanceRepository.saveInstance(newInstance)
        }
    }
}
