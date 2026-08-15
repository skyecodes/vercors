package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.repository.LoaderRepository
import arrow.core.raise.Raise
import dev.zacsweers.metro.Inject

@Inject
class GetAvailableLoadersUseCase(
    private val loaderRepository: LoaderRepository
) {
    context(_: Raise<LoaderError>)
    suspend operator fun invoke(gameVersion: String): List<ModLoaderType> =
        loaderRepository.getAvailableLoaders(gameVersion)
}
