package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.ManualDownloadItem
import app.vercors.feature.instance.domain.repository.InstanceRepository
import dev.zacsweers.metro.Inject

@Inject
class UpdateManualDownloadItemsUseCase(
    private val instanceRepository: InstanceRepository,
) {
    operator fun invoke(id: InstanceId, items: List<ManualDownloadItem>) =
        instanceRepository.updateManualDownloadItems(id, items)
}
