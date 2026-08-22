package app.vercors.shared.domain.model

data class JavaInstallation(
    val id: String,
    val path: String,
    val majorVersion: Int,
    val versionString: String,
    val vendor: String,
    val arch: String,
    val isManuallyAdded: Boolean = false,
)
