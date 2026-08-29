package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.value

private val logger = KotlinLogging.logger {}

@Inject
class CreateInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    context(_: Raise<InstanceValidationError>)
    suspend operator fun invoke(instance: Instance): Instance {
        val uniqueInstance = instance.copy(id = uniqueInstanceId(instance.id))
        instanceRepository.saveInstance(uniqueInstance)
        logger.info { "New instance created with name ${uniqueInstance.name} and ID ${uniqueInstance.id}" }
        return uniqueInstance
    }

    /** Appends `_1`, `_2`, ... to [id] until it no longer collides with an existing instance's ID. */
    private suspend fun uniqueInstanceId(id: InstanceId): InstanceId {
        val existingIds = instanceRepository.instances.now().value.keys
        if (id !in existingIds) return id
        var suffix = 1
        while ("${id}_$suffix" in existingIds) suffix++
        return "${id}_$suffix"
    }
}
