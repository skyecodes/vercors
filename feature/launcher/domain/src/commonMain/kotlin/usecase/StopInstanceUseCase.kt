package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class StopInstanceUseCase(
    private val instanceRepository: InstanceRepository,
) {
    suspend operator fun invoke(instanceId: InstanceId) {
        logger.info { "Stopping instance $instanceId" }
        instanceRepository.stopInstance(instanceId)
    }
}
