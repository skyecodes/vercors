package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceAndStatus
import app.vercors.feature.instance.domain.model.InstanceLoadingError
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.Either
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.combine
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.map

@Inject
class ObserveInstanceListUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(): ProgressiveFlow<Nothing, List<Either<InstanceLoadingError, InstanceAndStatus>>> =
        instanceRepository.instances.combine(instanceRepository.statuses) { instances, statuses ->
            instances.map { map ->
                map.map { (instanceId, either) ->
                    either.map { instance ->
                        InstanceAndStatus(
                            instance = instance,
                            status = statuses[instanceId] ?: InstanceStatus.Idle
                        )
                    }
                }
            }
        }
}
