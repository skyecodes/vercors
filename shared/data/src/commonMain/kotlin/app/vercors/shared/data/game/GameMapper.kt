package app.vercors.shared.data.game

import app.vercors.meta.lib.game.GameVersionDetailsData
import app.vercors.meta.lib.game.GameVersionListData
import app.vercors.meta.lib.game.GameVersionTypeData
import app.vercors.shared.domain.game.model.BriefGameVersion
import app.vercors.shared.domain.game.model.GameVersionDetails
import app.vercors.shared.domain.game.model.GameVersionList
import app.vercors.shared.domain.game.model.GameVersionType

internal fun GameVersionListData.toDomain(): GameVersionList = GameVersionList(
    latestRelease = latestRelease,
    latestSnapshot = latestSnapshot,
    versions = versions.map {
        BriefGameVersion(
            id = it.id,
            type = it.type.toDomain()
        )
    }
)

internal fun GameVersionTypeData.toDomain(): GameVersionType = when (this) {
    GameVersionTypeData.Release -> GameVersionType.Release
    GameVersionTypeData.Snapshot -> GameVersionType.Snapshot
    GameVersionTypeData.Beta -> GameVersionType.Beta
    GameVersionTypeData.Alpha -> GameVersionType.Alpha
}

internal fun GameVersionDetailsData.toDomain(): GameVersionDetails = GameVersionDetails(
    id = id,
    type = type.toDomain(),
    clientDownload = clientDownload.toDomain(),
    serverDownload = serverDownload?.toDomain(),
    libraries = libraries.map { it.toDomain() },
    javaVersion = javaVersion?.toDomain(),
    gameArgs = gameArgs?.map { it.toDomain() },
    jvmArgs = jvmArgs?.map { it.toDomain() },
    clientLogging = clientLogging?.toDomain(),
    mainClass = mainClass,
    assetsId = assetsId,
    assets = assets.map { it.toDomain() }
)

internal fun GameVersionDetailsData.Download.toDomain(): GameVersionDetails.Download = GameVersionDetails.Download(
    sha1 = sha1,
    size = size,
    url = url
)

internal fun GameVersionDetailsData.Library.toDomain(): GameVersionDetails.Library = GameVersionDetails.Library(
    name = name,
    artifact = artifact?.toDomain(),
    classifiers = classifiers?.mapValues { (_, value) -> value.toDomain() },
    natives = natives,
    extractExclude = extractExclude,
    rules = rules?.map { it.toDomain() }
)

internal fun GameVersionDetailsData.Library.Artifact.toDomain(): GameVersionDetails.Library.Artifact =
    GameVersionDetails.Library.Artifact(
        path = path,
        sha1 = sha1,
        size = size,
        url = url
    )

internal fun GameVersionDetailsData.Rule.toDomain(): GameVersionDetails.Rule = GameVersionDetails.Rule(
    action = action.toDomain(),
    features = features,
    os = os?.toDomain()
)

internal fun GameVersionDetailsData.Rule.Action.toDomain(): GameVersionDetails.Rule.Action = when (this) {
    GameVersionDetailsData.Rule.Action.Allow -> GameVersionDetails.Rule.Action.Allow
    GameVersionDetailsData.Rule.Action.Disallow -> GameVersionDetails.Rule.Action.Disallow
}

internal fun GameVersionDetailsData.Rule.Os.toDomain(): GameVersionDetails.Rule.Os = GameVersionDetails.Rule.Os(
    name = name,
    arch = arch,
    version = version
)

internal fun GameVersionDetailsData.JavaVersion.toDomain(): GameVersionDetails.JavaVersion =
    GameVersionDetails.JavaVersion(
        component = component,
        majorVersion = majorVersion
    )

internal fun GameVersionDetailsData.Argument.toDomain(): GameVersionDetails.Argument = when (this) {
    is GameVersionDetailsData.Argument.Basic -> GameVersionDetails.Argument.Basic(value)
    is GameVersionDetailsData.Argument.Conditional -> GameVersionDetails.Argument.Conditional(
        rules = rules.map { it.toDomain() },
        value = value
    )
}

internal fun GameVersionDetailsData.Logging.toDomain(): GameVersionDetails.Logging = GameVersionDetails.Logging(
    argument = argument,
    file = file.toDomain(),
    type = type.toDomain()
)

internal fun GameVersionDetailsData.Logging.File.toDomain(): GameVersionDetails.Logging.File =
    GameVersionDetails.Logging.File(
        id = id,
        sha1 = sha1,
        size = size,
        url = url
    )

internal fun GameVersionDetailsData.Logging.Type.toDomain(): GameVersionDetails.Logging.Type = when (this) {
    GameVersionDetailsData.Logging.Type.LOG4J2_XML -> GameVersionDetails.Logging.Type.LOG4J2_XML
}

internal fun GameVersionDetailsData.Asset.toDomain(): GameVersionDetails.Asset = GameVersionDetails.Asset(
    hash = hash,
    size = size
)
