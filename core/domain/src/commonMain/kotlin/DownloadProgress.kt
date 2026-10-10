package app.vercors.core.domain

import opensavvy.progress.Progress

data class DownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long?,
    val completedFiles: Int,
    val totalFiles: Int,
) : Progress.Loading.Quantified {
    override val normalized: Double
        get() {
            val ratio = when {
                totalBytes != null && totalBytes > 0 -> downloadedBytes.toDouble() / totalBytes
                totalFiles > 0 -> completedFiles.toDouble() / totalFiles
                else -> 0.0
            }
            return ratio.coerceIn(0.0, 1.0)
        }
}
