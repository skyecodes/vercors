package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class GetModpackManifestUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ModpackResolutionError>)
    suspend operator fun invoke(projectId: ProjectId, fileId: String): ModpackManifest =
        projectRepository.getModpackManifest(projectId, fileId)
}
