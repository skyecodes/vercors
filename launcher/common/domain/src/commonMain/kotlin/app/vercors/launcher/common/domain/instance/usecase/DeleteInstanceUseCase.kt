package app.vercors.launcher.common.domain.instance.usecase

import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceId
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
