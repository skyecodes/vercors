package app.vercors.launcher.common.domain.instance.usecase

import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.launcher.common.domain.instance.model.Instance
import org.koin.core.annotation.Factory

@Factory
class CreateInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instance: Instance) = instanceRepository.saveInstance(instance)
}