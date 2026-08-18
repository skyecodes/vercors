package app.vercors.feature.instance.data.content

import app.vercors.feature.instance.domain.model.InstalledContentFile

internal fun ContentEntry.toInstalledContentFile() = InstalledContentFile(
    fileName = fileName,
    title = title,
    description = description,
    version = version,
    loader = loader,
    fileSize = fileSize,
    iconFilePath = iconFilePath,
    providers = providers,
    enabled = enabled
)
