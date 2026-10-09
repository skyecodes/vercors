package app.vercors.shared.data

import app.vercors.core.filesystem.*
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.extractTo
import de.jonasbroeckmann.kzip.open
import kotlinx.io.files.Path
import kotlin.uuid.Uuid

// A single top-level directory is the world; otherwise the zip root is, named after the file
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

internal fun uniqueWorldFolderName(existingNames: Set<String>, base: String): String {
    if (base !in existingNames) return base
    var suffix = 2
    while ("$base ($suffix)" in existingNames) suffix++
    return "$base ($suffix)"
}
