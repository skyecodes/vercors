package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.RecordInstalledContentOriginUseCase
import app.vercors.feature.project.domain.model.modpack.InstalledModpackFile
import app.vercors.feature.project.domain.model.toContentKindOrNull
import dev.zacsweers.metro.Inject

@Inject
class RecordModpackFileOriginsUseCase(
    private val recordInstalledContentOrigin: RecordInstalledContentOriginUseCase
) {
    suspend operator fun invoke(
        instanceId: InstanceId,
        defaultPlatformId: String,
        installedFiles: List<InstalledModpackFile>
    ) {
        for (installedFile in installedFiles) {
            val kind = installedFile.kind.toContentKindOrNull() ?: continue
            val providerProjectId = installedFile.providerProjectId ?: continue
            val providerFileId = installedFile.providerFileId ?: continue
            recordInstalledContentOrigin(
                kind,
                instanceId,
                installedFile.fileName,
                installedFile.platformId ?: defaultPlatformId,
                providerProjectId,
                providerFileId
            )
        }
    }
}
