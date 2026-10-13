package app.vercors.feature.project.domain.model.pack

data class InstanceExportOptions(
    val referenceKnownFiles: Boolean = true,
    val versionId: String = "1.0.0",
    val summary: String? = null
)
