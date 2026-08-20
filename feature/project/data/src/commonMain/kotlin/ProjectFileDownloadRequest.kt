package app.vercors.feature.project.data

import app.vercors.feature.project.api.Platforms
import app.vercors.feature.project.api.ProjectFiles
import app.vercors.feature.project.api.Projects
import app.vercors.shared.api.ApiV1
import app.vercors.shared.data.FileValidationRequest
import kotlinx.io.files.Path
import opensavvy.spine.api.div
import opensavvy.spine.api.invoke
import opensavvy.spine.client.bodyOrThrow
import opensavvy.spine.client.request

internal fun projectFileDownloadRequest(
    platformId: String,
    projectId: String,
    fileId: String,
    path: Path,
    size: Long? = null,
    sha1: String? = null
): FileValidationRequest = FileValidationRequest.Channel(
    channel = { httpClient ->
        httpClient.request(
            endpoint = ApiV1 / Platforms / Platforms.PlatformId(platformId) / Projects / Projects.ProjectId(projectId) /
                    ProjectFiles / ProjectFiles.ProjectFileId(fileId) / ProjectFiles.ProjectFileId.download
        ).bodyOrThrow()
    },
    path = path,
    size = size,
    sha1 = sha1
)
