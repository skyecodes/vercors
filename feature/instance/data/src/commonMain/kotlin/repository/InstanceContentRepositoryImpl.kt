package app.vercors.feature.instance.data.repository

import app.vercors.feature.instance.data.content.ContentDirScanner
import app.vercors.feature.instance.data.content.InstanceResourcePackScanner
import app.vercors.feature.instance.data.content.InstanceShaderPackScanner
import app.vercors.feature.instance.data.mods.InstanceModScanner
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import opensavvy.state.coroutines.ProgressiveFlow

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceContentRepositoryImpl(
    private val modScanner: InstanceModScanner,
    private val resourcePackScanner: InstanceResourcePackScanner,
    private val shaderPackScanner: InstanceShaderPackScanner
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

    context(_: Raise<ImportError>)
    override suspend fun import(kind: ContentKind, instanceId: InstanceId, sourcePath: String): Unit =
        scannerFor(kind).importFile(instanceId, sourcePath)

    override suspend fun refresh(kind: ContentKind, instanceId: InstanceId): Unit = scannerFor(kind).refresh(instanceId)

    private fun scannerFor(kind: ContentKind): ContentDirScanner = when (kind) {
        ContentKind.Mod -> modScanner
        ContentKind.ResourcePack -> resourcePackScanner
        ContentKind.ShaderPack -> shaderPackScanner
    }
}
