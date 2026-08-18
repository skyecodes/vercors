package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstalledShaderPacksUseCase(
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    operator fun invoke(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        instanceShaderPackRepository.observeInstalledShaderPacks(instanceId)
}
