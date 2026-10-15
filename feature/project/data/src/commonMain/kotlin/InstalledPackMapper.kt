package app.vercors.feature.project.data

import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.project.data.dto.InstalledPackData
import app.vercors.feature.project.data.dto.InstalledPackFileData
import app.vercors.feature.project.data.dto.VercorsPackModpackInfo
import app.vercors.feature.project.domain.model.pack.InstalledPack
import app.vercors.feature.project.domain.model.pack.InstalledPackFile

internal fun InstalledPackData.toDomain(): InstalledPack = InstalledPack(
    name = name,
    versionName = versionName,
    modpackInfo = modpackInfo?.let { InstanceModpackInfo(it.platformId, it.projectId, it.fileId, it.versionName) },
    files = files.map { InstalledPackFile(it.path, it.platformId, it.projectId) },
)

internal fun InstalledPack.toData(): InstalledPackData = InstalledPackData(
    name = name,
    versionName = versionName,
    modpackInfo = modpackInfo?.let { VercorsPackModpackInfo(it.platformId, it.projectId, it.fileId, it.versionName) },
    files = files.map { InstalledPackFileData(it.path, it.platformId, it.projectId) },
)
