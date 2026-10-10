package app.vercors.feature.config.domain.model

import kotlin.jvm.JvmInline

sealed interface LauncherUpdateError {
    val message: String

    @JvmInline
    value class CheckFailed(override val message: String) : LauncherUpdateError

    @JvmInline
    value class DownloadFailed(override val message: String) : LauncherUpdateError
}
