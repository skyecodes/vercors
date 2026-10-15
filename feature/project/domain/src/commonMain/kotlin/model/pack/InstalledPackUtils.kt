package app.vercors.feature.project.domain.model.pack

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.localimport.isUserStatePath
import app.vercors.feature.project.domain.model.folderName
import app.vercors.feature.project.domain.model.modpack.ModpackManifest

data class PackContentFile(val kind: ContentKind, val fileName: String)

data class PackFileReplacement(
    val removed: List<String>,
    val toDisable: List<PackContentFile>,
)

// Content scanners only look at direct children of a content folder
fun packContentFileOrNull(path: String): PackContentFile? {
    val folder = path.substringBefore('/', missingDelimiterValue = "")
    val fileName = path.substringAfter('/')
    if (fileName.isEmpty() || '/' in fileName) return null
    val kind = ContentKind.entries.find { it.folderName == folder } ?: return null
    return PackContentFile(kind, fileName)
}

fun ModpackManifest.toInstalledPack(
    name: String,
    modpackInfo: InstanceModpackInfo?,
    overridePaths: List<String>
): InstalledPack = InstalledPack(
    name = name,
    versionName = modpackInfo?.versionName,
    modpackInfo = modpackInfo,
    files = files.map { InstalledPackFile(it.targetPath, it.platformId ?: platformId, it.providerProjectId) } +
            overridePaths.map { InstalledPackFile(it) }
).withoutUserState()

fun InstalledPack.withoutUserState(): InstalledPack =
    copy(files = files.filterNot { isUserStatePath(it.path) }.distinctBy { it.path })

fun InstalledPack.matches(pack: LocalPack): Boolean {
    val installed = modpackInfo
    val incoming = pack.modpackInfo
    if (installed != null && incoming != null) {
        return installed.platformId == incoming.platformId && installed.projectId == incoming.projectId
    }
    return name.trim().equals(pack.name.trim(), ignoreCase = true)
}

// A disabled file stays disabled in the new version, even when a version bump renamed it
fun planPackFileReplacement(
    old: InstalledPack?,
    new: InstalledPack,
    disabledOldPaths: Set<String>
): PackFileReplacement {
    val oldFiles = old?.files.orEmpty().filterNot { isUserStatePath(it.path) }
    val newPaths = new.files.mapTo(HashSet()) { it.path }
    val disabled = oldFiles.filter { it.path in disabledOldPaths }
    val disabledProjects = disabled.mapNotNullTo(HashSet()) { file -> file.projectId?.let { file.platformId to it } }
    val disabledContent = disabled.mapNotNullTo(HashSet()) { packContentFileOrNull(it.path) }
    val toDisable = new.files.mapNotNull { file ->
        packContentFileOrNull(file.path)?.takeIf { content ->
            content in disabledContent ||
                    (file.projectId != null && (file.platformId to file.projectId) in disabledProjects)
        }
    }
    return PackFileReplacement(
        removed = oldFiles.map { it.path }.filter { it !in newPaths },
        toDisable = toDisable
    )
}
