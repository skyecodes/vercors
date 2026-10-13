package app.vercors.feature.project.domain.repository

import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.export.InstanceExportError
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.project.domain.model.pack.PackFileReference
import app.vercors.feature.project.domain.model.pack.VercorsPackError
import app.vercors.feature.project.domain.model.pack.VercorsPackInfo
import arrow.core.raise.Raise
import opensavvy.progress.Progress

interface InstancePackRepository {
    context(_: Raise<InstanceExportError>)
    suspend fun writeMrpack(
        instance: Instance,
        references: List<PackFileReference>,
        overrides: List<InstanceExportFile>,
        versionId: String,
        summary: String?,
        targetPath: String,
        onProgress: (Progress) -> Unit = {}
    )

    context(_: Raise<InstanceExportError>)
    suspend fun writeVercorsPack(
        instance: Instance,
        references: List<PackFileReference>,
        overrides: List<InstanceExportFile>,
        targetPath: String,
        onProgress: (Progress) -> Unit = {}
    )

    context(_: Raise<VercorsPackError>)
    suspend fun parseVercorsPack(sourcePath: String): VercorsPackInfo

    suspend fun extractVercorsPackIcon(sourcePath: String, iconExtension: String): String?
}
