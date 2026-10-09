package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.ImportError
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
import arrow.core.raise.Raise
import opensavvy.state.coroutines.ProgressiveFlow

interface InstanceContentRepository {
    fun observeInstalledContent(
        kind: ContentKind,
        instanceId: InstanceId
    ): ProgressiveFlow<Nothing, List<InstalledContentFile>>

    suspend fun recordOrigin(
        kind: ContentKind,
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    )

    suspend fun setEnabled(kind: ContentKind, instanceId: InstanceId, fileName: String, enabled: Boolean)

    suspend fun remove(kind: ContentKind, instanceId: InstanceId, fileName: String)

    context(_: Raise<ImportError>)
    suspend fun import(kind: ContentKind, instanceId: InstanceId, sourcePath: String)

    suspend fun refresh(kind: ContentKind, instanceId: InstanceId)
}
