package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class CreateInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    context(_: Raise<InstanceValidationError>)
    suspend operator fun invoke(instance: Instance): Instance {
        val uniqueInstance = instance.copy(id = instanceRepository.uniqueInstanceId(instance.id))
        instanceRepository.saveInstance(uniqueInstance)
        logger.info { "New instance created with name ${uniqueInstance.name} and ID ${uniqueInstance.id}" }
        return uniqueInstance
    }
}
