package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.instance.InstanceRepository
import app.vercors.feature.instance.domain.model.Instance
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Factory

private val logger = KotlinLogging.logger {}

@Factory
class CreateInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    suspend operator fun invoke(instance: Instance) {
        instanceRepository.saveInstance(instance)
        logger.info { "New instance created with name ${instance.name} and ID ${instance.id}" }
    }
}
