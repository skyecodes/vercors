package app.vercors.feature.game.domain.repository

import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import opensavvy.state.coroutines.ProgressiveFlow

interface LoaderRepository {
    fun observeAvailableLoaders(gameVersion: String): ProgressiveFlow<LoaderError, List<ModLoaderType>>

    fun observeLoaderVersions(
        gameVersion: String,
        loader: ModLoaderType
    ): ProgressiveFlow<LoaderError, List<LoaderVersion>>
}
