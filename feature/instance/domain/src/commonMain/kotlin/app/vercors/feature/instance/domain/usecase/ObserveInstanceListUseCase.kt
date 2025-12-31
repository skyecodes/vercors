package app.vercors.feature.instance.domain.usecase

import app.vercors.core.domain.ResourceStateFlow
import app.vercors.feature.instance.domain.instance.InstanceRepository
import app.vercors.feature.instance.domain.model.Instance
import org.koin.core.annotation.Factory

@Factory
class ObserveInstanceListUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(): ResourceStateFlow<List<Instance>> = instanceRepository.instances
}
