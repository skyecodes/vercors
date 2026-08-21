package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class CancelInstanceSetupUseCase(
    private val instanceRepository: InstanceRepository
) {
    operator fun invoke(instanceId: InstanceId) {
        logger.info { "Cancelling setup for instance $instanceId" }
        instanceRepository.cancelInstanceJob(instanceId)
    }
}
