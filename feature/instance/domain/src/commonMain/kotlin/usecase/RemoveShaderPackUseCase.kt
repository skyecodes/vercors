package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject

@Inject
class RemoveShaderPackUseCase(
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, fileName: String) =
        instanceShaderPackRepository.removeShaderPack(instanceId, fileName)
}
