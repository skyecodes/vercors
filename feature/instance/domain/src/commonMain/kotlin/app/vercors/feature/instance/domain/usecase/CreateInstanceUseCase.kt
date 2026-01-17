package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class CreateInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instance: Instance) {
        instanceRepository.saveInstance(instance)
        logger.info { "New instance created with name ${instance.name} and ID ${instance.id}" }
    }
}
