package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstalledServer
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceServerRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveInstanceServersUseCase(
    private val instanceServerRepository: InstanceServerRepository
) {
    operator fun invoke(instanceId: InstanceId): ProgressiveFlow<Nothing, List<InstalledServer>> =
        instanceServerRepository.observeServers(instanceId)
}
