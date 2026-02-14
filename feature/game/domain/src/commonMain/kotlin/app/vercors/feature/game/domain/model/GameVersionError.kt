package app.vercors.feature.game.domain.model

import kotlin.jvm.JvmInline

sealed interface GameVersionError {
    @JvmInline
    value class InvalidManifest(val cause: Any) : GameVersionError

    @JvmInline
    value class InvalidDetails(val cause: Any) : GameVersionError

    @JvmInline
    value class InvalidAssetIndex(val cause: Any) : GameVersionError

    @JvmInline
    value class NotFound(val versionId: String) : GameVersionError
}
