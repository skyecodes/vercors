package app.vercors.feature.project.domain.model.modpack

data class ModpackInstallResult(
    val files: List<InstalledModpackFile>,
    val overridePaths: List<String>,
)
