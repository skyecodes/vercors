package app.vercors.feature.project.domain.usecase

import app.vercors.feature.project.domain.model.pack.VercorsPackError
import app.vercors.feature.project.domain.model.pack.VercorsPackInfo
import app.vercors.feature.project.domain.repository.InstancePackRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class ParseVercorsPackUseCase(
    private val instancePackRepository: InstancePackRepository
) {
    context(_: Raise<VercorsPackError>)
    suspend operator fun invoke(sourcePath: String): VercorsPackInfo =
        instancePackRepository.parseVercorsPack(sourcePath)
}
