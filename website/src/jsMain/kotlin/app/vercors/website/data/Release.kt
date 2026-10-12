package app.vercors.website.data

class Release(
    val version: String,
    val url: String,
    val publishedAt: String?,
    val isPrerelease: Boolean,
    val downloads: List<DownloadOption>,
)
