package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.repository.LoaderRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveAvailableLoadersUseCase(
    private val loaderRepository: LoaderRepository
) {
    operator fun invoke(gameVersion: String): ProgressiveFlow<LoaderError, List<ModLoaderType>> =
        loaderRepository.observeAvailableLoaders(gameVersion)
}
