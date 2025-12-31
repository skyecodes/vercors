package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.instance.InstanceRepository
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import org.koin.core.annotation.Factory

@Factory
class DeleteInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instanceId: InstanceId): Instance {
        val instance = instanceRepository.findInstance(instanceId)
        instanceRepository.deleteInstance(instance)
        return instance
    }
}
