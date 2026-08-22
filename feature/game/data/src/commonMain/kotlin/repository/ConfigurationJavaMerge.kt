package app.vercors.feature.game.data.repository

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.shared.domain.model.JavaInstallation

/** Merges [newInstallations] into this configuration's [Configuration.javaInstallations], skipping any that already
 *  exist (by id) so a rescan never wipes/overwrites existing or manually-added entries. For every major version
 *  touched by the merge, if it now has exactly one candidate and no existing valid preference, that candidate
 *  becomes the preferred installation for that major version. */
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
