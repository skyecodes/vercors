package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.content.ContentDirScannerService
import app.vercors.feature.instance.data.content.InstanceResourcePackScannerService
import app.vercors.feature.instance.data.content.InstanceShaderPackScannerService
import app.vercors.feature.instance.data.mods.InstanceModScannerService
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceContentRepositoryImpl(
    private val modScannerService: InstanceModScannerService,
    private val resourcePackScannerService: InstanceResourcePackScannerService,
    private val shaderPackScannerService: InstanceShaderPackScannerService
) : InstanceContentRepository {
    override fun observeInstalledContent(
        kind: ContentKind,
        instanceId: InstanceId
    ): ProgressiveFlow<Nothing, List<InstalledContentFile>> = scannerFor(kind).observe(instanceId)

    override suspend fun recordOrigin(
        kind: ContentKind,
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ): Unit = scannerFor(kind).recordOrigin(instanceId, fileName, platformId, projectId, fileId)

    override suspend fun setEnabled(
        kind: ContentKind,
        instanceId: InstanceId,
        fileName: String,
        enabled: Boolean
    ): Unit = scannerFor(kind).setEnabled(instanceId, fileName, enabled)

    override suspend fun remove(kind: ContentKind, instanceId: InstanceId, fileName: String): Unit =
        scannerFor(kind).removeFile(instanceId, fileName)

    override suspend fun import(kind: ContentKind, instanceId: InstanceId, sourcePath: String): Boolean =
        scannerFor(kind).importFile(instanceId, sourcePath)

    override suspend fun refresh(kind: ContentKind, instanceId: InstanceId): Unit = scannerFor(kind).refresh(instanceId)

    private fun scannerFor(kind: ContentKind): ContentDirScannerService = when (kind) {
        ContentKind.Mod -> modScannerService
        ContentKind.ResourcePack -> resourcePackScannerService
        ContentKind.ShaderPack -> shaderPackScannerService
    }
}
