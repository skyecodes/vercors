package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class ParseLocalModpackUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ModpackResolutionError>)
    suspend operator fun invoke(sourcePath: String, platformId: String): ModpackManifest =
        projectRepository.parseLocalModpack(sourcePath, platformId)
}
