package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class DeleteInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    context(_: Raise<InstanceError>)
    suspend operator fun invoke(instanceId: InstanceId): Instance {
        val instance = instanceRepository.findInstance(instanceId)
        instanceRepository.deleteInstance(instance)
        return instance
    }
}
