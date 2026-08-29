package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject

@Inject
class SetContentEnabledUseCase(
    private val instanceModRepository: InstanceModRepository,
    private val instanceResourcePackRepository: InstanceResourcePackRepository,
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    suspend operator fun invoke(kind: ContentKind, instanceId: InstanceId, fileName: String, enabled: Boolean) =
        when (kind) {
            ContentKind.Mod -> instanceModRepository.setModEnabled(instanceId, fileName, enabled)
            ContentKind.ResourcePack ->
                instanceResourcePackRepository.setResourcePackEnabled(instanceId, fileName, enabled)

            ContentKind.ShaderPack -> instanceShaderPackRepository.setShaderPackEnabled(instanceId, fileName, enabled)
        }
}
