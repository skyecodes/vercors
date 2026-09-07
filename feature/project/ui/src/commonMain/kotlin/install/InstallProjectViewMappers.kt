package app.vercors.feature.project.ui.install

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectType

internal fun Instance.toInstallableInstanceViewOrNull(
    projectType: ProjectType,
    compatibleLoaders: List<ModLoaderType>
): InstallableInstanceView? {
    if (projectType.requiresLoader) {
        val loader = modLoader ?: return null
        if (loader.type !in compatibleLoaders) return null
        return InstallableInstanceView(
            id = id,
            name = name,
            gameVersion = gameVersion,
            loaderType = loader.type,
            loaderVersion = loader.version,
            updatedAt = updatedAt
        )
    }
    return InstallableInstanceView(
        id = id,
        name = name,
        gameVersion = gameVersion,
        loaderType = modLoader?.type,
        loaderVersion = modLoader?.version,
        updatedAt = updatedAt
    )
}

internal fun ProjectFile.toView(): ProjectFileView = ProjectFileView(
    id = id,
    displayName = displayName,
    fileName = fileName,
    gameVersions = gameVersions,
    releaseChannel = releaseChannel,
    size = size,
    sha1 = sha1,
    md5 = md5,
    publishedAt = publishedAt,
    dependencies = dependencies,
    manualDownloadUrl = manualDownloadUrl,
    loaders = loaders
)
