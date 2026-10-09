package app.vercors.feature.game.backend.loader

import app.vercors.feature.game.api.dto.LoaderErrorData
import app.vercors.feature.game.api.dto.LoaderVersionData
import arrow.core.raise.Raise

interface LoaderServiceBase {
    context(_: Raise<LoaderErrorData>)
    suspend fun getVersions(gameVersion: String): List<LoaderVersionData>

    context(_: Raise<LoaderErrorData>)
    suspend fun getInstallUrl(gameVersion: String, loaderVersion: String): String
}
