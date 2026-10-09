package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import dev.zacsweers.metro.Inject

@Inject
class RecordInstalledContentOriginUseCase(
    private val instanceContentRepository: InstanceContentRepository
) {
    suspend operator fun invoke(
        kind: ContentKind,
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ): Unit = instanceContentRepository.recordOrigin(kind, instanceId, fileName, platformId, projectId, fileId)
}
