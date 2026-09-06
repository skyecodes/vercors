package app.vercors.shared.data

import app.vercors.core.filesystem.*
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.extractTo
import de.jonasbroeckmann.kzip.open
import kotlinx.io.files.Path
import kotlin.uuid.Uuid

/**
 * Extracts [zipPath] (a zip of a Minecraft save folder — either downloaded from a platform or picked locally for
 * import) into [instanceId]'s `saves/` directory as a new, uniquely-named world. Unlike every other content type,
 * the archive isn't a single file to move into place — it's extracted wholesale to a scratch directory first, then
 * the resulting layout is inspected: if extraction produced exactly one top-level directory, that directory *is*
 * the world (its own name is used, e.g. an export shaped like `<WorldName>/level.dat`); otherwise the zip's root
 * itself is the world root (`level.dat` at the top), and [sourceFileName] (minus `.zip`) is used instead. Never
 * deletes [zipPath] itself — that's the caller's responsibility.
 */
suspend fun AppPaths.extractWorldZip(zipPath: Path, instanceId: String, sourceFileName: String) {
    val savesDir = savesDir(instanceId)
    savesDir.createDirectories()
    val scratchDir = Path(downloadsTmp, "world-extract-${Uuid.random()}")
    Zip.open(zipPath).use { zip -> zip.extractTo(scratchDir) }
    try {
        val singleTopLevelDir = scratchDir.list().singleOrNull()?.takeIf { it.isDirectory() }
        val (sourceDir, baseName) = if (singleTopLevelDir != null) {
            singleTopLevelDir to singleTopLevelDir.name
        } else {
            scratchDir to sourceFileName.removeSuffix(".zip")
        }
        val existingNames = savesDir.list().map { it.name }.toSet()
        sourceDir.moveTo(worldDir(instanceId, uniqueWorldFolderName(existingNames, baseName)))
    } finally {
        if (scratchDir.exists()) scratchDir.deleteRecursively()
    }
}

/** Dedupes [base] against [existingNames] by appending " (2)", " (3)", ... until it no longer collides. */
fun uniqueWorldFolderName(existingNames: Set<String>, base: String): String {
    if (base !in existingNames) return base
    var suffix = 2
    while ("$base ($suffix)" in existingNames) suffix++
    return "$base ($suffix)"
}
