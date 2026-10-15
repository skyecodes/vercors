package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.project.domain.repository.InstanceUpdateRepository
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

private val AUTO_CHECK_INTERVAL = 1.hours

@Inject
class ShouldAutoCheckInstanceUpdatesUseCase(
    private val instanceUpdateRepository: InstanceUpdateRepository
) {
    operator fun invoke(instanceId: InstanceId, instanceOverride: Boolean?, globalEnabled: Boolean): Boolean {
        if (!(instanceOverride ?: globalEnabled)) return false
        val checkedAt = instanceUpdateRepository.get(instanceId)?.checkedAt ?: return true
        return Clock.System.now() - checkedAt >= AUTO_CHECK_INTERVAL
    }
}
