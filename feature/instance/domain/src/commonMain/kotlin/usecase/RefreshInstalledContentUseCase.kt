package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject

@Inject
class RefreshInstalledContentUseCase(
    private val instanceModRepository: InstanceModRepository,
    private val instanceResourcePackRepository: InstanceResourcePackRepository,
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    suspend operator fun invoke(kind: ContentKind, instanceId: InstanceId) = when (kind) {
        ContentKind.Mod -> instanceModRepository.refreshInstalledMods(instanceId)
        ContentKind.ResourcePack -> instanceResourcePackRepository.refreshInstalledResourcePacks(instanceId)
        ContentKind.ShaderPack -> instanceShaderPackRepository.refreshInstalledShaderPacks(instanceId)
    }
}
