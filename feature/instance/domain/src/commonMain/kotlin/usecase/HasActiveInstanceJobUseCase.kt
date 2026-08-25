package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject

@Inject
class HasActiveInstanceJobUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(instanceId: InstanceId): Boolean = instanceRepository.hasActiveJob(instanceId)
}
