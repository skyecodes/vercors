package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.ModProvider
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectFileMatch
import app.vercors.feature.project.domain.model.pack.PackFileReference

internal const val MODRINTH_PLATFORM_ID = "modrinth"

internal const val CURSEFORGE_PLATFORM_ID = "curseforge"

// The only hosts the Modrinth App accepts in a .mrpack's downloads list
private val MRPACK_ALLOWED_HOSTS = setOf("cdn.modrinth.com", "github.com", "raw.githubusercontent.com", "gitlab.com")

// A recorded origin is only trusted once the platform confirms the same file, downloadable without a browser
internal fun selectVerifiedReferences(
    platformId: String,
    origins: List<Pair<InstanceExportFile, ModProvider>>,
    remoteFiles: Map<String, ProjectFile>
): List<PackFileReference> = origins.mapNotNull { (file, provider) ->
    val sha1 = file.sha1 ?: return@mapNotNull null
    val remote = remoteFiles[provider.fileId]
        ?.takeIf { it.sha1 == sha1 && it.manualDownloadUrl == null }
        ?: return@mapNotNull null
    PackFileReference(
        relativePath = file.relativePath,
        platformId = platformId,
        projectId = provider.projectId,
        fileId = provider.fileId,
        sha1 = sha1,
        sha512 = remote.sha512,
        size = file.size,
        downloadUrl = remote.downloadUrl
    )
}

internal fun selectHashMatchReferences(
    platformId: String,
    files: List<InstanceExportFile>,
    matches: Map<String, ProjectFileMatch>,
    requireMrpackHost: Boolean
): List<PackFileReference> = files.mapNotNull { file ->
    val sha1 = file.sha1 ?: return@mapNotNull null
    val match = matches[sha1]?.takeIf { it.file.manualDownloadUrl == null } ?: return@mapNotNull null
    if (requireMrpackHost && (match.file.sha512 == null || !match.file.downloadUrl.isMrpackAllowedUrl())) {
        return@mapNotNull null
    }
    PackFileReference(
        relativePath = file.relativePath,
        platformId = platformId,
        projectId = match.projectId.id,
        fileId = match.file.id,
        sha1 = sha1,
        sha512 = match.file.sha512,
        size = file.size,
        downloadUrl = match.file.downloadUrl
    )
}

private fun String?.isMrpackAllowedUrl(): Boolean =
    this != null && startsWith("https://") && removePrefix("https://").substringBefore('/') in MRPACK_ALLOWED_HOSTS
