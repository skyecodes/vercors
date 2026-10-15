package app.vercors.feature.project.domain.model

import app.vercors.core.domain.DownloadProgress

data class ProjectInstallProgress(
    val fraction: Float,
    val download: DownloadProgress? = null,
)
