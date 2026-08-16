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

/**
 * A loader's install artifact (Fabric/Quilt's `profile/json`, Forge/NeoForge's installer jar) is fetched
 * through the meta API rather than an upstream URL hardcoded here: the meta API resolves the real, possibly
 * loader-specific upstream location and redirects to it, so this client never needs to know it.
 */
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
