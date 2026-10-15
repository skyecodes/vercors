package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.model.update.InstanceUpdates
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
class ObserveInstanceUpdatesUseCase(
    private val instanceUpdateRepository: InstanceUpdateRepository
) {
    operator fun invoke(): Flow<Map<InstanceId, InstanceUpdates>> = instanceUpdateRepository.observeAll()

    operator fun invoke(instanceId: InstanceId): Flow<InstanceUpdates?> =
        instanceUpdateRepository.observeAll().map { it[instanceId] }.distinctUntilChanged()
}
