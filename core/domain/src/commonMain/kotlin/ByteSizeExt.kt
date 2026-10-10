package app.vercors.core.domain

private val SIZE_UNITS = listOf("KB", "MB", "GB", "TB")

fun Long.toHumanReadableSize(): String {
    if (this < 1024) return "$this B"
    var value = this / 1024.0
    var unitIndex = 0
    while (value >= 1024 && unitIndex < SIZE_UNITS.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "${(value * 10).toInt() / 10.0} ${SIZE_UNITS[unitIndex]}"
}
