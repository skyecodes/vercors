package app.vercors.feature.game.backend.loader

private val VERSION_PART_REGEX = Regex("\\d+|\\D+")

internal val versionComparator = Comparator<String> { a, b ->
    val partsA = VERSION_PART_REGEX.findAll(a).map { it.value }.toList()
    val partsB = VERSION_PART_REGEX.findAll(b).map { it.value }.toList()
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
