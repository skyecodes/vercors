package app.vercors.feature.instance.domain.repository

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstalledContentFile
import app.vercors.feature.instance.domain.model.InstanceId
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

    suspend fun import(kind: ContentKind, instanceId: InstanceId, sourcePath: String): Boolean

    suspend fun refresh(kind: ContentKind, instanceId: InstanceId)
}
