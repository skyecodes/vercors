package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstalledWorld
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceWorldRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstalledWorldsUseCase(
    private val instanceWorldRepository: InstanceWorldRepository
) {
    operator fun invoke(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledWorld>> =
        instanceWorldRepository.observeInstalledWorlds(instanceId)
}
