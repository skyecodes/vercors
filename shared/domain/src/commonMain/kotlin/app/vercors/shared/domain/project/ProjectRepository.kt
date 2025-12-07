package app.vercors.shared.domain.project

import app.vercors.shared.domain.Result
import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.Project
import app.vercors.shared.domain.project.model.ProjectPlatform
import app.vercors.shared.domain.project.model.ProjectType

interface ProjectRepository {
    suspend fun getFeaturedProjectList(platform: ProjectPlatform): Result<Map<ProjectType, List<BriefProject>>>
    suspend fun getProject(platform: ProjectPlatform, id: String): Result<Project>
}
