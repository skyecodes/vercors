package app.vercors.feature.project.data

import app.vercors.feature.project.domain.model.ProjectType
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.ContentCacheKind
import kotlinx.io.files.Path

internal fun AppPaths.contentDir(type: ProjectType, instanceId: String): Path = when (type) {
    ProjectType.Mod, ProjectType.Modpack -> modsDir(instanceId)
    ProjectType.ResourcePack -> resourcePacksDir(instanceId)
    ProjectType.ShaderPack -> shaderPacksDir(instanceId)
    ProjectType.DataPack, ProjectType.World -> error("Install is not supported for project type $type")
}

internal fun ProjectType.toContentCacheKind(): ContentCacheKind? = when (this) {
    ProjectType.Mod, ProjectType.Modpack -> ContentCacheKind.Mod
    ProjectType.ResourcePack -> ContentCacheKind.ResourcePack
    ProjectType.ShaderPack -> ContentCacheKind.ShaderPack
    ProjectType.DataPack, ProjectType.World -> null
}
