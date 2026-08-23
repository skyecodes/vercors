package app.vercors.feature.game.domain.usecase

import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.game.domain.repository.LoaderRepository
import dev.zacsweers.metro.Inject
import opensavvy.state.coroutines.ProgressiveFlow

@Inject
class ObserveLoaderVersionsUseCase(
    private val loaderRepository: LoaderRepository
) {
    operator fun invoke(gameVersion: String, loader: ModLoaderType): ProgressiveFlow<LoaderError, List<LoaderVersion>> =
        loaderRepository.observeLoaderVersions(gameVersion, loader)
}
