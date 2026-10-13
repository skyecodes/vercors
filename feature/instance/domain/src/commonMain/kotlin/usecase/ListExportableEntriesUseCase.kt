package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.export.ExportableEntry
import app.vercors.feature.instance.domain.repository.InstanceExportRepository
import dev.zacsweers.metro.Inject

@Inject
class ListExportableEntriesUseCase(
    private val instanceExportRepository: InstanceExportRepository
) {
    suspend operator fun invoke(instanceId: InstanceId): List<ExportableEntry> =
        instanceExportRepository.listExportableEntries(instanceId)
}
