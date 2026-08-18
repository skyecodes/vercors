package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstalledResourcePacksUseCase(
    private val instanceResourcePackRepository: InstanceResourcePackRepository
) {
    operator fun invoke(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledContentFile>> =
        instanceResourcePackRepository.observeInstalledResourcePacks(instanceId)
}
