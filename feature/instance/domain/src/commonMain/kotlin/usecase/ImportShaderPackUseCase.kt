package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject

@Inject
class ImportShaderPackUseCase(
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    suspend operator fun invoke(instanceId: InstanceId, sourcePath: String): Boolean =
        instanceShaderPackRepository.importShaderPack(instanceId, sourcePath)
}
