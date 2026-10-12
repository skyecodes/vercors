package app.vercors.website.data

private val RECOMMENDED_FORMATS = mapOf(
    Os.Windows to DownloadFormat.Exe,
    Os.MacOs to DownloadFormat.Dmg,
    Os.Linux to DownloadFormat.AppImage,
)

fun List<DownloadOption>.recommendedFor(platform: Platform): DownloadOption? {
    val os = platform.os ?: return null
    val format = RECOMMENDED_FORMATS.getValue(os)
    val candidates = filter { it.kind == DownloadKind.Gui && it.os == os && it.format == format }
    return candidates.firstOrNull { it.arch == platform.arch } ?: candidates.firstOrNull()
}

fun Long.toReadableSize(): String = when {
    this >= 1_000_000 -> "${(this / 100_000) / 10.0} MB"
    this >= 1_000 -> "${this / 1_000} kB"
    else -> "$this B"
}
