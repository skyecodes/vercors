package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstalledContentUseCase(
    private val instanceModRepository: InstanceModRepository,
    private val instanceResourcePackRepository: InstanceResourcePackRepository,
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    operator fun invoke(
        kind: ContentKind,
        instanceId: InstanceId
    ): ProgressiveFlow<Nothing, List<InstalledContentFile>> = when (kind) {
        ContentKind.Mod -> instanceModRepository.observeInstalledMods(instanceId)
        ContentKind.ResourcePack -> instanceResourcePackRepository.observeInstalledResourcePacks(instanceId)
        ContentKind.ShaderPack -> instanceShaderPackRepository.observeInstalledShaderPacks(instanceId)
    }
}
