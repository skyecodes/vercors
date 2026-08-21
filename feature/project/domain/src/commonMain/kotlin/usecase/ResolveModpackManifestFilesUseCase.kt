package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackResolutionError
import app.vercors.feature.project.domain.repository.ProjectRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject
import opensavvy.progress.Progress

@Inject
class ResolveModpackManifestFilesUseCase(
    private val projectRepository: ProjectRepository
) {
    context(_: Raise<ModpackResolutionError>)
    suspend operator fun invoke(
        manifest: ModpackManifest,
        onProgress: (Progress) -> Unit = {}
    ): ModpackManifest = projectRepository.resolveModpackManifestFiles(manifest, onProgress)
}
