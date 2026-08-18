package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstalledModsUseCase(
    private val instanceModRepository: InstanceModRepository
) {
    operator fun invoke(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        instanceModRepository.observeInstalledMods(instanceId)
}
