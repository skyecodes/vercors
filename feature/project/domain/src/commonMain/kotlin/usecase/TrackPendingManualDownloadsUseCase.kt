package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.domain.usecase.UpdateInstanceUseCase
import app.vercors.feature.instance.domain.usecase.UpdateManualDownloadItemsUseCase
import app.vercors.feature.project.domain.model.ManualDownloadCandidate
import app.vercors.feature.project.domain.model.toPendingManualDownload
import arrow.core.raise.context.either
import dev.zacsweers.metro.Inject
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

@Inject
class TrackPendingManualDownloadsUseCase(
    private val updateManualDownloadItems: UpdateManualDownloadItemsUseCase,
    private val updateInstance: UpdateInstanceUseCase
) {
    // Best-effort: must never fail the surrounding install
    suspend operator fun invoke(
        instanceId: InstanceId,
        candidates: List<ManualDownloadCandidate>,
        items: List<ManualDownloadItem>
    ) {
        updateManualDownloadItems(instanceId, items)
        val pending = if (items.isEmpty()) {
            emptyList()
        } else {
            val done = items.filter { it.downloaded }.map { it.fileName }.toSet()
            candidates.filterNot { it.fileName in done }.map { it.toPendingManualDownload() }
        }
        either { updateInstance(instanceId) { it.copy(pendingManualDownloads = pending) } }
            .onLeft { error -> logger.warn { "Failed to persist pending manual downloads for $instanceId: $error" } }
    }
}
