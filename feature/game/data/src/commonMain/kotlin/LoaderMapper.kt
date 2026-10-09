package app.vercors.feature.game.data

import app.vercors.feature.game.api.dto.LoaderTypeData
import app.vercors.feature.game.api.dto.LoaderVersionData
import app.vercors.feature.game.data.dto.FabricLoaderProfile
import app.vercors.feature.game.data.dto.LegacyForgeInstallProfile
import app.vercors.feature.game.data.dto.LoaderVersionProfile
import app.vercors.feature.game.data.loader.mavenCoordinateToPath
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.LoaderInstallProfile
import app.vercors.feature.game.domain.model.LoaderVersion
import app.vercors.feature.game.domain.model.ModLoaderType

private const val DEFAULT_LEGACY_LIBRARY_BASE_URL = "https://libraries.minecraft.net/"

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

// The installer's own coordinate is listed with a URL that 404s: it's extracted locally instead (see ForgeFamilyInstall)
internal fun LegacyForgeInstallProfile.toDomain(): LoaderInstallProfile {
    val universalLibrary = GameVersionDetails.Library(
        name = install.path,
        artifact = GameVersionDetails.Library.Artifact(
            path = mavenCoordinateToPath(install.path),
            sha1 = null,
            size = null,
            url = ""
        )
    )
    return LoaderInstallProfile(
        mainClass = versionInfo.mainClass,
        libraries = versionInfo.libraries.filterNot { it.name == install.path }
            .map { it.toDomain() } + universalLibrary,
        gameArgs = versionInfo.minecraftArguments?.toBasicArguments(),
        jvmArgs = null,
        // minecraftArguments is already complete: replace, never append, or every flag is duplicated
        replacesGameArgs = true
    )
}

internal fun LegacyForgeInstallProfile.Library.toDomain(): GameVersionDetails.Library {
    val path = mavenCoordinateToPath(name)
    return GameVersionDetails.Library(
        name = name,
        artifact = GameVersionDetails.Library.Artifact(
            path = path,
            sha1 = null,
            size = null,
            url = "${(url ?: DEFAULT_LEGACY_LIBRARY_BASE_URL).trimEnd('/')}/$path"
        )
    )
}
