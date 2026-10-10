package app.vercors.feature.config.domain.model

fun Configuration.withoutJavaInstallations(ids: Set<String>): Configuration {
    val removed = javaInstallations.filter { it.id in ids }
    if (removed.isEmpty()) return this
    val remaining = javaInstallations - removed.toSet()
    val preferred = removed.map { it.majorVersion }.distinct().fold(preferredJavaByMajorVersion) { acc, majorVersion ->
        when (acc[majorVersion]) {
            null, !in ids -> acc
            else -> remaining.firstOrNull { it.majorVersion == majorVersion }
                ?.let { acc + (majorVersion to it.id) }
                ?: (acc - majorVersion)
        }
    }
    return copy(javaInstallations = remaining, preferredJavaByMajorVersion = preferred)
}
