package app.vercors.feature.game.backend.loader

private val versionPartRegex = Regex("\\d+|\\D+")

// Splits into alternating numeric/non-numeric runs and compares them piecewise (numeric runs as
// numbers, others lexically); a shorter, otherwise-equal version wins since it has no qualifier suffix.
internal val versionComparator = Comparator<String> { a, b ->
    val partsA = versionPartRegex.findAll(a).map { it.value }.toList()
    val partsB = versionPartRegex.findAll(b).map { it.value }.toList()
    var result = 0
    for (i in 0 until minOf(partsA.size, partsB.size)) {
        val partA = partsA[i]
        val partB = partsB[i]
        result = if (partA[0].isDigit() && partB[0].isDigit()) {
            partA.toLong().compareTo(partB.toLong())
        } else {
            partA.compareTo(partB)
        }
        if (result != 0) break
    }
    if (result == 0) partsB.size.compareTo(partsA.size) else result
}
