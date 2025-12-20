package app.vercors.shared.data.project

import app.vercors.meta.lib.project.BriefProjectData
import app.vercors.meta.lib.project.ProjectData
import app.vercors.meta.lib.project.ProjectTypeData
import app.vercors.shared.domain.project.model.BriefProject
import app.vercors.shared.domain.project.model.Project
import app.vercors.shared.domain.project.model.ProjectId
import app.vercors.shared.domain.project.model.ProjectType

internal fun ProjectTypeData.toDomain(): ProjectType = when (this) {
    ProjectTypeData.Mod -> ProjectType.Mod
    ProjectTypeData.Modpack -> ProjectType.Modpack
    ProjectTypeData.ResourcePack -> ProjectType.ResourcePack
    ProjectTypeData.ShaderPack -> ProjectType.ShaderPack
    ProjectTypeData.DataPack -> ProjectType.DataPack
    ProjectTypeData.World -> ProjectType.World
}

internal fun BriefProjectData.toDomain(): BriefProject = BriefProject(
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

internal fun ProjectData.toDomain(): Project = Project(
    id = ProjectId(id, platform),
    name = name,
    description = description,
    body = body,
    url = url,
    logoUrl = logoUrl
)
