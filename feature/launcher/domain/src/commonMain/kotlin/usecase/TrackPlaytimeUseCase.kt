package app.vercors.feature.launcher.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import arrow.core.raise.context.either
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val logger = KotlinLogging.logger {}

private val PLAYTIME_TICK_INTERVAL = 30.seconds

// Runs until cancelled; recomputes from the session baseline so a delayed or duplicated tick never double-counts
@Inject
class TrackPlaytimeUseCase(
    private val updateInstance: UpdateInstanceUseCase,
) {
    suspend operator fun invoke(instanceId: InstanceId, baselinePlayTime: Duration) {
        val sessionStart = Clock.System.now()
        try {
            persistPlaytime(instanceId, baselinePlayTime, sessionStart)
            while (true) {
                delay(PLAYTIME_TICK_INTERVAL)
                persistPlaytime(instanceId, baselinePlayTime, sessionStart)
            }
        } finally {
            withContext(NonCancellable) { persistPlaytime(instanceId, baselinePlayTime, sessionStart) }
        }
    }

    private suspend fun persistPlaytime(instanceId: InstanceId, baselinePlayTime: Duration, sessionStart: Instant) {
        val now = Clock.System.now()
        either {
            updateInstance(instanceId) {
                it.copy(
                    playTime = baselinePlayTime + (now - sessionStart),
                    lastPlayedAt = now
                )
            }
        }.onLeft { error -> logger.warn { "Failed to persist playtime for $instanceId: $error" } }
    }
}
