package app.vercors.launcher.common.domain.instance.usecase

import app.vercors.launcher.common.domain.instance.InstanceRepository
import app.vercors.launcher.common.domain.instance.model.Instance
import app.vercors.shared.domain.ResourceStateFlow
import org.koin.core.annotation.Factory

@Factory
class ObserveInstanceListUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(): ResourceStateFlow<List<Instance>> = instanceRepository.instances
}