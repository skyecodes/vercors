package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import dev.zacsweers.metro.Inject

@Inject
class SetModEnabledUseCase(
    private val instanceModRepository: InstanceModRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, fileName: String, enabled: Boolean) =
        instanceModRepository.setModEnabled(instanceId, fileName, enabled)
}
