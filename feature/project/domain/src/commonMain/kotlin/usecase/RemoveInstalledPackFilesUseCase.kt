package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.repository.InstanceContentRepository
import app.vercors.feature.project.domain.model.pack.packContentFileOrNull
import app.vercors.feature.project.domain.repository.InstalledPackRepository
import dev.zacsweers.metro.Inject

@Inject
class RemoveInstalledPackFilesUseCase(
    private val instanceContentRepository: InstanceContentRepository,
    private val installedPackRepository: InstalledPackRepository,
) {
    suspend operator fun invoke(instanceId: InstanceId, paths: List<String>) {
        for (path in paths) {
            val content = packContentFileOrNull(path)
            if (content != null) instanceContentRepository.remove(content.kind, instanceId, content.fileName)
            else installedPackRepository.deleteGameFile(instanceId, path)
        }
    }
}
