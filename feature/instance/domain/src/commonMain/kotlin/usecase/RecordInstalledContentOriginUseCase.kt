package app.vercors.feature.instance.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceModRepository
import app.vercors.feature.instance.domain.repository.InstanceResourcePackRepository
import app.vercors.feature.instance.domain.repository.InstanceShaderPackRepository
import dev.zacsweers.metro.Inject

@Inject
class RecordInstalledContentOriginUseCase(
    private val instanceModRepository: InstanceModRepository,
    private val instanceResourcePackRepository: InstanceResourcePackRepository,
    private val instanceShaderPackRepository: InstanceShaderPackRepository
) {
    suspend operator fun invoke(
        kind: ContentKind,
        instanceId: InstanceId,
        fileName: String,
        platformId: String,
        projectId: String,
        fileId: String
    ): Unit = when (kind) {
        ContentKind.Mod -> instanceModRepository.recordModOrigin(instanceId, fileName, platformId, projectId, fileId)
        ContentKind.ResourcePack -> instanceResourcePackRepository.recordResourcePackOrigin(
            instanceId,
            fileName,
            platformId,
            projectId,
            fileId
        )

        ContentKind.ShaderPack -> instanceShaderPackRepository.recordShaderPackOrigin(
            instanceId,
            fileName,
            platformId,
            projectId,
            fileId
        )
    }
}
