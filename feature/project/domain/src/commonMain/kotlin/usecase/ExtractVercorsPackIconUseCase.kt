package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.repository.InstancePackRepository
import dev.zacsweers.metro.Inject

@Inject
class ExtractVercorsPackIconUseCase(
    private val instancePackRepository: InstancePackRepository
) {
    suspend operator fun invoke(sourcePath: String, iconExtension: String): String? =
        instancePackRepository.extractVercorsPackIcon(sourcePath, iconExtension)
}
