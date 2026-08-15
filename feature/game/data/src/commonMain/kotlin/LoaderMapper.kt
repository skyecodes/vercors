package app.vercors.feature.game.data

import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType

internal fun LoaderTypeData.toDomain(): ModLoaderType = when (this) {
    LoaderTypeData.Fabric -> ModLoaderType.Fabric
    LoaderTypeData.Quilt -> ModLoaderType.Quilt
    LoaderTypeData.Forge -> ModLoaderType.Forge
    LoaderTypeData.NeoForge -> ModLoaderType.NeoForge
}

internal fun ModLoaderType.toApiData(): LoaderTypeData = when (this) {
    ModLoaderType.Fabric -> LoaderTypeData.Fabric
    ModLoaderType.Quilt -> LoaderTypeData.Quilt
    ModLoaderType.Forge -> LoaderTypeData.Forge
    ModLoaderType.NeoForge -> LoaderTypeData.NeoForge
}

internal fun LoaderVersionData.toDomain(): LoaderVersion = LoaderVersion(version = version, stable = stable)
