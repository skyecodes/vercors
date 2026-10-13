package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.export.ExportableEntry
import app.vercors.feature.instance.domain.model.export.InstanceExportError
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import arrow.core.raise.Raise
import opensavvy.progress.Progress

interface InstanceExportRepository {
    suspend fun listExportableEntries(instanceId: InstanceId): List<ExportableEntry>

    context(_: Raise<InstanceExportError>)
    suspend fun collectFiles(
        instanceId: InstanceId,
        entries: Set<String>,
        hashContent: Boolean,
        onProgress: (Progress) -> Unit = {}
    ): List<InstanceExportFile>

    context(_: Raise<InstanceExportError>)
    suspend fun writePrismExport(
        instance: Instance,
        files: List<InstanceExportFile>,
        targetPath: String,
        onProgress: (Progress) -> Unit = {}
    )
}
