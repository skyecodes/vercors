package app.vercors.feature.java.data.repository

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.shared.domain.model.JavaInstallation

// Never overwrites existing entries, so a rescan keeps manually-added installations
internal fun Configuration.mergeJavaInstallations(newInstallations: List<JavaInstallation>): Configuration {
    val existingIds = javaInstallations.map { it.id }.toSet()
    val toAdd = newInstallations.filter { it.id !in existingIds }
    if (toAdd.isEmpty()) return this

    val merged = javaInstallations + toAdd
    val byMajorVersion = merged.groupBy { it.majorVersion }
    var preferred = preferredJavaByMajorVersion
    for (majorVersion in toAdd.map { it.majorVersion }.distinct()) {
        val candidates = byMajorVersion[majorVersion].orEmpty()
        val stillValid = candidates.any { it.id == preferred[majorVersion] }
        if (candidates.size == 1 && !stillValid) {
            preferred = preferred + (majorVersion to candidates.first().id)
        }
    }
    return copy(javaInstallations = merged, preferredJavaByMajorVersion = preferred)
}
