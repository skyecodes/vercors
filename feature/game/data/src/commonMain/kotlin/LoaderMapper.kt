package app.vercors.feature.game.data

import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.feature.game.data.dto.FabricLoaderProfile
import app.vercors.feature.game.data.dto.LoaderVersionProfile
import app.vercors.feature.game.data.loader.mavenCoordinateToPath
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
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

internal fun LoaderVersionProfile.toDomain(): LoaderInstallProfile = LoaderInstallProfile(
    mainClass = mainClass,
    libraries = libraries.map { it.toDomain() },
    gameArgs = arguments?.game?.map { it.toDomain() },
    jvmArgs = arguments?.jvm?.map { it.toDomain() }
)

internal fun FabricLoaderProfile.toDomain(): LoaderInstallProfile = LoaderInstallProfile(
    mainClass = mainClass,
    libraries = libraries.map { it.toDomain() },
    gameArgs = arguments?.game?.map { it.toDomain() },
    jvmArgs = arguments?.jvm?.map { it.toDomain() }
)

internal fun FabricLoaderProfile.Library.toDomain(): GameVersionDetails.Library {
    val path = mavenCoordinateToPath(name)
    return GameVersionDetails.Library(
        name = name,
        artifact = GameVersionDetails.Library.Artifact(
            path = path,
            sha1 = sha1,
            size = size,
            url = "${url.trimEnd('/')}/$path"
        )
    )
}
