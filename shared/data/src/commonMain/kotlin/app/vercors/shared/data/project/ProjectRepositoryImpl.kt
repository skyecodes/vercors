package app.vercors.shared.data.project

import app.vercors.meta.client.project.ProjectApi
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.project.ProjectRepository
import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.Project
import app.vercors.shared.domain.project.model.ProjectPlatform
import app.vercors.shared.domain.project.model.ProjectType
import org.koin.core.annotation.Single

@Single
internal class ProjectRepositoryImpl(
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
