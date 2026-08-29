package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject

@Inject
class ImportContentUseCase(
    private val instanceModRepository: InstanceModRepository,
    private val instanceResourcePackRepository: InstanceResourcePackRepository,
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    suspend operator fun invoke(kind: ContentKind, instanceId: InstanceId, sourcePath: String): Boolean = when (kind) {
        ContentKind.Mod -> instanceModRepository.importMod(instanceId, sourcePath)
        ContentKind.ResourcePack -> instanceResourcePackRepository.importResourcePack(instanceId, sourcePath)
        ContentKind.ShaderPack -> instanceShaderPackRepository.importShaderPack(instanceId, sourcePath)
    }
}
