package app.vercors.feature.project.domain.model.pack

import app.vercors.feature.instance.domain.model.InstanceModpackInfo

data class InstalledPack(
    val name: String,
    val versionName: String?,
    val modpackInfo: InstanceModpackInfo?,
    val files: List<InstalledPackFile>,
)

data class InstalledPackFile(
    val path: String,
    val platformId: String? = null,
    val projectId: String? = null,
)
