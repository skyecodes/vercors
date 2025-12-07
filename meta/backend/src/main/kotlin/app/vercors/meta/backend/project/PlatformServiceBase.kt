package app.vercors.meta.backend.project

import app.vercors.meta.lib.project.PlatformData
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectSearchResponseData
import app.vercors.meta.lib.project.ProjectTypeData

interface PlatformServiceBase {
    val info: PlatformData

    suspend fun search(
        type: ProjectTypeData,
        query: String? = null,
        limit: Int? = null
    ): ProjectSearchResponseData

    suspend fun getProject(projectId: String): ProjectData
}
