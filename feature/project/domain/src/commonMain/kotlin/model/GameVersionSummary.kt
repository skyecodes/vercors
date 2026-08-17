package app.vercors.feature.project.domain.model

private val stableVersionRegex = Regex("""^(\d+)\.(\d+)(?:\.(\d+))?$""")

private data class ParsedVersion(val major: Int, val minor: Int, val patch: Int?, val raw: String)

fun List<String>.summarizeStableGameVersions(): List<String> {
    val parsed = mapNotNull { raw ->
        stableVersionRegex.matchEntire(raw)?.let {
            val (major, minor, patch) = it.destructured
            ParsedVersion(major.toInt(), minor.toInt(), patch.toIntOrNull(), raw)
        }
    }

    return parsed
        .groupBy { it.major to it.minor }
        .entries
        .sortedWith(
            compareByDescending<Map.Entry<Pair<Int, Int>, List<ParsedVersion>>> { it.key.first }
                .thenByDescending { it.key.second }
        )
        .map { (key, versions) ->
            val (major, minor) = key
            val hasBare = versions.any { it.patch == null }
            val patched = versions.filter { it.patch != null }.sortedBy { it.patch }
            when {
                hasBare && versions.size > 1 -> "$major.$minor.x"
                patched.size > 1 -> "${patched.first().raw}-${patched.last().raw}"
                else -> versions.first().raw
            }
        }
}
