package app.vercors.feature.game.domain.model

data class JavaProbeResult(
    val versionString: String,
    val majorVersion: Int,
    val vendor: String,
    val arch: String,
)

sealed interface JavaInstallationError {
    data class NotAJavaInstallation(val path: String) : JavaInstallationError
}
