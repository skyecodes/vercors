package app.vercors.shared.data.project

import app.vercors.meta.client.project.ProjectApi
import app.vercors.meta.client.project.createProjectApi
import app.vercors.meta.lib.project.BriefProjectData
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectTypeData
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.project.ProjectRepository
import app.vercors.shared.domain.project.model.*
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Single

@Single
class ProjectRepositoryImpl(
    private val projectApi: ProjectApi
) : ProjectRepository {
    override suspend fun getFeaturedProjectList(
        platform: ProjectPlatform
    ): Result<Map<ProjectType, List<BriefProject>>> = projectApi.getFeaturedProjectList(platform.id).map { res ->
        res.entries.associate { (key, value) ->
            key.toDomain() to value.map { it.toDomain() }
        }
    }.toResult()

    override suspend fun getProject(
        platform: ProjectPlatform,
        id: String
    ): Result<Project> = projectApi.getProject(platform.id, id).map { it.toDomain() }.toResult()
}

private fun ProjectTypeData.toDomain(): ProjectType = when (this) {
    ProjectTypeData.Mod -> ProjectType.Mod
    ProjectTypeData.Modpack -> ProjectType.Modpack
    ProjectTypeData.ResourcePack -> ProjectType.ResourcePack
    ProjectTypeData.ShaderPack -> ProjectType.ShaderPack
}

private fun BriefProjectData.toDomain(): BriefProject = BriefProject(
    id = ProjectId(id, platform),
    name = name,
    description = description,
    author = author,
    url = url,
    downloads = downloads,
    lastUpdated = lastUpdated,
    logoUrl = logoUrl,
    imageUrl = imageUrl,
)

private fun ProjectData.toDomain(): Project = Project(
    id = ProjectId(id, platform),
    name = name,
    description = description,
    body = body,
    url = url,
    logoUrl = logoUrl
)

@Single
internal fun provideProjectApi(ktorfit: Ktorfit): ProjectApi = ktorfit.createProjectApi()
