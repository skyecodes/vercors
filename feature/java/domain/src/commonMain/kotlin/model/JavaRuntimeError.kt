package app.vercors.feature.java.domain.model

import kotlin.jvm.JvmInline

sealed interface JavaRuntimeError {
    @JvmInline
    value class InvalidManifest(val cause: Any) : JavaRuntimeError

    @JvmInline
    value class ComponentNotAvailable(val majorVersion: Int) : JavaRuntimeError

    @JvmInline
    value class DownloadFailed(val cause: Any) : JavaRuntimeError

    data object ProbeFailed : JavaRuntimeError
}
