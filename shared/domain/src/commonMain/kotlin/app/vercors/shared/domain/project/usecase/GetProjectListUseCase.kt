package app.vercors.shared.domain.project.usecase

import app.vercors.shared.domain.Result
import app.vercors.shared.domain.project.ProjectRepository
import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.PlatformId
import app.vercors.shared.domain.project.model.ProjectType
import org.koin.core.annotation.Factory

@Factory
class GetProjectListUseCase(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(
        platformId: PlatformId,
        type: ProjectType,
        query: String?,
        categories: List<String>?,
        pageSize: Int?
    ): Result<List<BriefProject>> = projectRepository.getProjectList(
        platformId = platformId,
        type = type,
        query = query,
        categories = categories,
        pageSize = pageSize,
    )
}
