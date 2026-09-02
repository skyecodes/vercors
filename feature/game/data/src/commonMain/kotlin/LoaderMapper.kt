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

/**
 * Unlike modern Forge/NeoForge, legacy Forge does no client-jar binary-patching, so `omitClientFromClasspath`
 * stays `false` (default): the vanilla client jar must remain on the classpath, unmodified, alongside the
 * universal jar.
 *
 * `versionInfo.libraries` typically also lists the loader's own coordinate ([LegacyForgeInstallProfile.Install.path])
 * as if it were a normal downloadable library, with a real-looking maven `url` — it isn't actually fetchable
 * there (confirmed 404 against the real maven for a real build). `ForgeFamilyInstall.kt`'s legacy branch
 * extracts that coordinate's real content — the "universal" jar bundled inside the installer jar itself —
 * directly to that exact (unclassified) library path before this mapping runs, so that self-referencing entry
 * is filtered out of the normal per-library download list here and replaced with a single locally-produced,
 * never-fetched (`url = ""`) one instead.
 */
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
        // versionInfo.minecraftArguments is already a complete, self-sufficient args list (it textually
        // repeats every standard flag itself) — must replace, not append to, the vanilla version's own
        // (also minecraftArguments-derived) gameArgs, or every flag ends up duplicated. See the doc comment
        // on LoaderInstallProfile.replacesGameArgs.
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
