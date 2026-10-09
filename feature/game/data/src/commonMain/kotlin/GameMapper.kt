package app.vercors.feature.game.data

import app.vercors.feature.game.data.dto.MojangAssetIndex
import app.vercors.feature.game.data.dto.MojangReleaseType
import app.vercors.feature.game.data.dto.MojangVersionInfo
import app.vercors.feature.game.data.dto.MojangVersionManifest
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionList
import app.vercors.feature.game.domain.model.GameVersionType

internal fun MojangVersionManifest.toDomain(): GameVersionList = GameVersionList(
    latestRelease = latest.release,
    latestSnapshot = latest.snapshot,
    versions = versions.map {
        GameVersionList.Version(
            id = it.id,
            type = it.type.toDomain(),
            url = it.url,
            sha1 = it.sha1
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
    // Pre-1.13 version JSON only has the legacy minecraftArguments string
    gameArgs = arguments?.game?.map { it.toDomain() } ?: minecraftArguments?.toBasicArguments(),
    jvmArgs = arguments?.jvm?.map { it.toDomain() },
    clientLogging = logging?.client?.toDomain(),
    mainClass = mainClass,
    assets = assets,
    assetIndex = assetIndex.toDomain()
)

internal fun String.toBasicArguments(): List<GameVersionDetails.Argument> =
    trim().split(Regex("\\s+")).filter { it.isNotBlank() }.map { GameVersionDetails.Argument.Basic(it) }

internal fun MojangVersionInfo.Downloads.Download.toDomain(): GameVersionDetails.Download = GameVersionDetails.Download(
    sha1 = sha1,
    size = size,
    url = url
)

internal fun MojangVersionInfo.Library.toDomain(): GameVersionDetails.Library = GameVersionDetails.Library(
    name = name,
    artifact = downloads?.artifact?.toDomain(),
    classifiers = downloads?.classifiers?.mapValues { (_, value) -> value.toDomain() },
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

internal fun MojangAssetIndex.toDomain(): GameAssetIndex = GameAssetIndex(
    objects = objects.mapValues { it.value.toDomain() }
)

internal fun MojangAssetIndex.AssetObject.toDomain(): GameAssetIndex.AssetObject = GameAssetIndex.AssetObject(
    hash = hash,
    size = size
)
