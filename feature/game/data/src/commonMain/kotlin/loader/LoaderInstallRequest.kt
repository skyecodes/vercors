package app.vercors.feature.game.data.loader

import app.vercors.feature.game.api.Game
import app.vercors.feature.game.api.Loaders
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.FileValidationRequest
import kotlinx.io.files.Path
import opensavvy.spine.api.div
import opensavvy.spine.api.invoke
import opensavvy.spine.client.bodyOrThrow
import opensavvy.spine.client.request

// Fetched via the meta API, which redirects to the loader-specific upstream
internal fun loaderInstallArtifactRequest(
    gameVersionId: String,
    loaderId: String,
    loaderVersion: String,
    path: Path
): FileValidationRequest = FileValidationRequest.Channel(
    channel = { httpClient ->
        httpClient.request(
            endpoint = ApiV1 / Game / Game.GameVersionId(gameVersionId) / Loaders / Loaders.LoaderId(loaderId) /
                    Loaders.LoaderId.installUrl,
            parameters = { this.loaderVersion = loaderVersion }
        ).bodyOrThrow()
    },
    path = path
)
