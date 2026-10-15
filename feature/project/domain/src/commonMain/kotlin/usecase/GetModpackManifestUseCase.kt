package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.repository.ModpackRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import opensavvy.progress.Progress

@Inject
class GetModpackManifestUseCase(
    private val modpackRepository: ModpackRepository
) {
    context(_: Raise<ModpackResolutionError>)
    suspend operator fun invoke(
        projectId: ProjectId,
        fileId: String,
        onProgress: (Progress) -> Unit = {}
    ): ModpackManifest =
        modpackRepository.getModpackManifest(projectId, fileId, onProgress)
}
