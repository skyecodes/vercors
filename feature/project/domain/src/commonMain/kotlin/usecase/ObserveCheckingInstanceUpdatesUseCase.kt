package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class ObserveCheckingInstanceUpdatesUseCase(
    private val instanceUpdateRepository: InstanceUpdateRepository
) {
    operator fun invoke(instanceId: InstanceId): Flow<Boolean> =
        instanceUpdateRepository.observeChecking().map { instanceId in it }.distinctUntilChanged()
}
