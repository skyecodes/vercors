package app.vercors.feature.instance.domain.usecase

import app.vercors.core.domain.toSlug
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceError
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.InstanceValidationError
import app.vercors.feature.instance.domain.repository.InstanceRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Clock
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

@Inject
class CopyInstanceUseCase(
    private val instanceRepository: InstanceRepository
) {
    context(_: Raise<InstanceError>, _: Raise<InstanceValidationError>)
    suspend operator fun invoke(sourceId: InstanceId, newName: String): Instance {
        val source = instanceRepository.findInstance(sourceId)
        val newInstance = source.copy(
            name = newName,
            id = instanceRepository.uniqueInstanceId(newName.toSlug()),
            pendingManualDownloads = emptyList(),
            installStage = null,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            lastPlayedAt = null,
            playTime = Duration.ZERO,
        )
        instanceRepository.copyInstanceFiles(sourceId, newInstance.id)
        instanceRepository.saveInstance(newInstance)
        logger.info { "Copied instance ${source.id} to ${newInstance.id}" }
        return newInstance
    }
}
