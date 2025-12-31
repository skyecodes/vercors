package app.vercors.feature.game.data

import app.vercors.feature.game.domain.model.BriefGameVersion
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.model.GameVersionType
import app.vercors.meta.lib.game.MojangReleaseType
import app.vercors.meta.lib.game.MojangVersionInfo
import app.vercors.meta.lib.game.MojangVersionManifest

internal fun MojangVersionManifest.toDomain(): GameVersionList = GameVersionList(
    latestRelease = latest.release,
    latestSnapshot = latest.snapshot,
    versions = versions.map {
        BriefGameVersion(
            id = it.id,
            type = it.type.toDomain()
        )
    }
)

internal fun MojangReleaseType.toDomain(): GameVersionType = when (this) {
    MojangReleaseType.Release -> GameVersionType.Release
    MojangReleaseType.Snapshot -> GameVersionType.Snapshot
    MojangReleaseType.Beta -> GameVersionType.Beta
    MojangReleaseType.Alpha -> GameVersionType.Alpha
}

internal fun MojangVersionInfo.toDomain(): GameVersionDetails = GameVersionDetails(
    id = id,
    type = type.toDomain(),
    clientDownload = downloads.client.toDomain(),
    serverDownload = downloads.server?.toDomain(),
    libraries = libraries.map { it.toDomain() },
    javaVersion = javaVersion?.toDomain(),
    gameArgs = arguments?.game?.map { it.toDomain() },
    jvmArgs = arguments?.jvm?.map { it.toDomain() },
    clientLogging = logging?.client?.toDomain(),
    mainClass = mainClass,
    assets = assets,
    assetIndex = assetIndex.toDomain()
)

internal fun MojangVersionInfo.Downloads.Download.toDomain(): GameVersionDetails.Download = GameVersionDetails.Download(
    sha1 = sha1,
    size = size,
    url = url
)

internal fun MojangVersionInfo.Library.toDomain(): GameVersionDetails.Library = GameVersionDetails.Library(
    name = name,
    artifact = downloads.artifact?.toDomain(),
    classifiers = downloads.classifiers?.mapValues { (_, value) -> value.toDomain() },
    natives = natives,
    extractExclude = extract?.exclude,
    rules = rules?.map { it.toDomain() }
)

internal fun MojangVersionInfo.Library.Downloads.Artifact.toDomain(): GameVersionDetails.Library.Artifact =
    GameVersionDetails.Library.Artifact(
        path = path,
        sha1 = sha1,
        size = size,
        url = url
    )

internal fun MojangVersionInfo.Rule.toDomain(): GameVersionDetails.Rule = GameVersionDetails.Rule(
    action = action.toDomain(),
    features = features,
    os = os?.toDomain()
)

internal fun MojangVersionInfo.Rule.Action.toDomain(): GameVersionDetails.Rule.Action = when (this) {
    MojangVersionInfo.Rule.Action.Allow -> GameVersionDetails.Rule.Action.Allow
    MojangVersionInfo.Rule.Action.Disallow -> GameVersionDetails.Rule.Action.Disallow
}

internal fun MojangVersionInfo.Rule.Os.toDomain(): GameVersionDetails.Rule.Os = GameVersionDetails.Rule.Os(
    name = name,
    arch = arch,
    version = version
)

internal fun MojangVersionInfo.JavaVersion.toDomain(): GameVersionDetails.JavaVersion =
    GameVersionDetails.JavaVersion(
        component = component,
        majorVersion = majorVersion
    )

internal fun MojangVersionInfo.Argument.toDomain(): GameVersionDetails.Argument = when (this) {
    is MojangVersionInfo.Argument.Basic -> GameVersionDetails.Argument.Basic(value)
    is MojangVersionInfo.Argument.Conditional -> GameVersionDetails.Argument.Conditional(
        rules = rules.map { it.toDomain() },
        value = value
    )
}

internal fun MojangVersionInfo.Logging.Client.toDomain(): GameVersionDetails.Logging = GameVersionDetails.Logging(
    argument = argument,
    file = file.toDomain(),
    type = type.toDomain()
)

internal fun MojangVersionInfo.Logging.File.toDomain(): GameVersionDetails.Logging.File =
    GameVersionDetails.Logging.File(
        id = id,
        sha1 = sha1,
        size = size,
        url = url
    )

internal fun MojangVersionInfo.Logging.Type.toDomain(): GameVersionDetails.Logging.Type = when (this) {
    MojangVersionInfo.Logging.Type.Log4j2Xml -> GameVersionDetails.Logging.Type.LOG4J2_XML
}

internal fun MojangVersionInfo.AssetIndex.toDomain(): GameVersionDetails.AssetIndex = GameVersionDetails.AssetIndex(
    id = id,
    sha1 = sha1,
    size = size,
    totalSize = totalSize,
    url = url
)
