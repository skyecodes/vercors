package app.vercors.core.filesystem

import ca.gosyer.appdirs.AppDirs
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

fun defaultAppRoot(appId: String): Path = Path(AppDirs { appName = appId }.getUserDataDir())

fun Path.dataDirectoryMarkerFile(): Path = Path(this, "data-directory.txt")

fun readAppRoot(defaultRoot: Path): Path? {
    val file = defaultRoot.dataDirectoryMarkerFile()
    if (!SystemFileSystem.exists(file)) return null
    return runCatching {
        SystemFileSystem.source(file).buffered().use { it.readString() }
    }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }?.let(::Path)
}

fun writeAppRoot(defaultRoot: Path, root: Path) {
    SystemFileSystem.createDirectories(defaultRoot)
    SystemFileSystem.sink(defaultRoot.dataDirectoryMarkerFile()).buffered().use { it.writeString(root.toString()) }
}

fun ensureRootReachable(root: Path): Boolean = runCatching {
    SystemFileSystem.createDirectories(root)
}.isSuccess

fun withAppFolderName(appId: String, path: Path): Path =
    if (path.name == appId) path else Path(path, appId)
