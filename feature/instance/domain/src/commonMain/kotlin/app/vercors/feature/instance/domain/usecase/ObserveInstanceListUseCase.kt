package app.vercors.feature.instance.domain.usecase

import app.vercors.core.domain.ResourceFlow
import app.vercors.core.domain.map
import app.vercors.feature.instance.domain.model.InstanceAndStatus
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.combine

@Inject
class ObserveInstanceListUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(): ResourceFlow<List<InstanceAndStatus>> =
        instanceRepository.instances.combine(instanceRepository.statuses) { instances, statuses ->
            instances.map { list ->
                list.map {
                    InstanceAndStatus(
                        instance = it,
                        status = statuses[it.id] ?: InstanceStatus.Idle
                    )
                }
            }
        }
}
