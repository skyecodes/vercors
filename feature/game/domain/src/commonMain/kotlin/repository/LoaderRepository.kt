package app.vercors.feature.game.domain.repository

import app.vercors.feature.game.domain.model.LoaderError
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType
import arrow.core.raise.Raise

interface LoaderRepository {
    context(_: Raise<LoaderError>)
    suspend fun getAvailableLoaders(gameVersion: String): List<ModLoaderType>

    context(_: Raise<LoaderError>)
    suspend fun getLoaderVersions(gameVersion: String, loader: ModLoaderType): List<LoaderVersion>
}
