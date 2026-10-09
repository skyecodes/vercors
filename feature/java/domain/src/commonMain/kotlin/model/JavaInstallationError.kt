package app.vercors.feature.java.domain.model

import kotlin.jvm.JvmInline

sealed interface JavaInstallationError {
    @JvmInline
    value class NotAJavaInstallation(val path: String) : JavaInstallationError
}
