package app.vercors.feature.project.ui.install

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.project.domain.model.ProjectFile

internal fun Instance.toInstallableInstanceViewOrNull(compatibleLoaders: List<ModLoaderType>): InstallableInstanceView? {
    val loader = modLoader ?: return null
    if (loader.type !in compatibleLoaders) return null
    return InstallableInstanceView(
        id = id,
        name = name,
        gameVersion = gameVersion,
        loaderType = loader.type,
        loaderVersion = loader.version
    )
}

internal fun ProjectFile.toView(): ProjectFileView = ProjectFileView(
    id = id,
    displayName = displayName,
    fileName = fileName,
    gameVersions = gameVersions,
    releaseChannel = releaseChannel,
    publishedAt = publishedAt
)
