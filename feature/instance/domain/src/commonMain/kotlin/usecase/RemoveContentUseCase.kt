package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject

@Inject
class RemoveContentUseCase(
    private val instanceModRepository: InstanceModRepository,
    private val instanceResourcePackRepository: InstanceResourcePackRepository,
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    suspend operator fun invoke(kind: ContentKind, instanceId: InstanceId, fileName: String): Unit = when (kind) {
        ContentKind.Mod -> instanceModRepository.removeMod(instanceId, fileName)
        ContentKind.ResourcePack -> instanceResourcePackRepository.removeResourcePack(instanceId, fileName)
        ContentKind.ShaderPack -> instanceShaderPackRepository.removeShaderPack(instanceId, fileName)
    }
}
