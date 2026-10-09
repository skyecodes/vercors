package app.vercors.feature.java.domain.model

data class JavaProbeResult(
    val versionString: String,
    val majorVersion: Int,
    val vendor: String,
    val arch: String,
)
