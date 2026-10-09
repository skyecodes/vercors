package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Job

@Inject
class UpdateInstanceJobUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(instanceId: InstanceId, job: Job?): Unit =
        instanceRepository.updateInstanceJob(instanceId, job)
}
