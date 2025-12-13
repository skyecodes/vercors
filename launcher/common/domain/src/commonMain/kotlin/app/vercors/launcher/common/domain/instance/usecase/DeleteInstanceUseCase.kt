package app.vercors.launcher.common.domain.instance.usecase

import app.vercors.launcher.common.domain.instance.InstanceNotFoundException
import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.launcher.common.domain.instance.model.InstanceId
import app.vercors.shared.domain.awaitValueOrThrow
import org.koin.core.annotation.Factory

@Factory
class DeleteInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instanceId: InstanceId): Instance {
        val instances = instanceRepository.instances.awaitValueOrThrow()
        val instance = instances.find { it.id == instanceId } ?: throw InstanceNotFoundException(instanceId)
        instanceRepository.deleteInstance(instance)
        return instance
    }
}
