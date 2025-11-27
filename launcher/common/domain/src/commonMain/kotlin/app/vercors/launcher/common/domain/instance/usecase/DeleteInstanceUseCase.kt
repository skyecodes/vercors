package app.vercors.launcher.common.domain.instance.usecase

import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.shared.domain.awaitValueOrThrow
import org.koin.core.annotation.Factory

@Factory
class DeleteInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instanceId: String): Boolean {
        val instances = instanceRepository.instances.awaitValueOrThrow()
        return instances.find { it.id == instanceId }?.let {
            instanceRepository.deleteInstance(it)
            true
        } ?: false
    }
}