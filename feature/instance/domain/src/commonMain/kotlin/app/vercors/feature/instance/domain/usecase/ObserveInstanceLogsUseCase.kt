package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceLogEvent
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
class ObserveInstanceLogsUseCase(
    private val instanceRepository: InstanceRepository,
) {
    operator fun invoke(id: InstanceId): Flow<List<InstanceLogEvent>?> = instanceRepository.logs.map { it[id] }
}
