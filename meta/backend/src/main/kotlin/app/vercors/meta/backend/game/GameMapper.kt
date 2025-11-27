package app.vercors.meta.backend.game

import app.vercors.meta.backend.game.mojang.dto.*
import app.vercors.meta.lib.game.GameVersionDetails
import app.vercors.meta.lib.game.GameVersionList
import app.vercors.meta.lib.game.GameVersionType
import kotlin.time.toKotlinInstant

internal fun MojangVersionManifest.convert(): GameVersionList = GameVersionList(
    latestRelease = latest.release,
    latestSnapshot = latest.snapshot,
    versions = versions.sortedBy { it.releaseTime }.reversed().map {
        GameVersionList.Version(
            id = it.id,
            type = it.type.convert(),
        )
    }
)

private fun MojangReleaseType.convert(): GameVersionType = when (this) {
    MojangReleaseType.Release -> GameVersionType.Release
    MojangReleaseType.Snapshot -> GameVersionType.Snapshot
    MojangReleaseType.Beta -> GameVersionType.Beta
    MojangReleaseType.Alpha -> GameVersionType.Alpha
}

internal fun MojangVersionInfo.convert(): GameVersionDetails = GameVersionDetails(
    id = id,
    gameArgs = arguments?.game?.map { it.convert() },
    jvmArgs = arguments?.jvm?.map { it.convert() },
    assetIndex = assetIndex.convert(),
    clientDownload = downloads.client.convert(),
    serverDownload = downloads.server?.convert(),
    javaVersion = javaVersion?.convert(),
    libraries = libraries.map { it.convert() },
    clientLogging = logging?.client?.convert(),
    mainClass = mainClass,
    minimumLauncherVersion = minimumLauncherVersion,
    releaseTime = releaseTime.toKotlinInstant(),
    type = type.convert()
)

private fun MojangVersionInfo.Logging.File.convert(): GameVersionDetails.Logging.File = GameVersionDetails.Logging.File(
    id = id,
    sha1 = sha1,
    size = size,
    url = url,
)

private fun MojangVersionInfo.Logging.Client.convertType(): GameVersionDetails.Logging.Type =
    when (type) {
        "log4j2-xml" -> GameVersionDetails.Logging.Type.LOG4J2_XML
        else -> error("Unknown logging type $type")
    }

private fun MojangVersionInfo.Logging.Client.convert(): GameVersionDetails.Logging = GameVersionDetails.Logging(
    argument = argument,
    file = file.convert(),
    type = convertType()
)

private fun MojangVersionInfo.Library.convert(): GameVersionDetails.Library =
    GameVersionDetails.Library(
        name = name,
        artifact = downloads.artifact?.convert(),
        rules = rules?.map { it.convert() }
    )

private fun MojangVersionInfo.Library.Downloads.Artifact.convert(): GameVersionDetails.Library.Artifact =
    GameVersionDetails.Library.Artifact(
        path = path,
        sha1 = sha1,
        size = size,
        url = url
    )

private fun MojangVersionInfo.JavaVersion.convert(): GameVersionDetails.JavaVersion = GameVersionDetails.JavaVersion(
    component = component,
    majorVersion = majorVersion
)

private fun MojangVersionInfo.Downloads.Download.convert(): GameVersionDetails.Download = GameVersionDetails.Download(
    sha1 = sha1,
    size = size,
    url = url
)

private fun MojangVersionInfo.AssetIndex.convert(): GameVersionDetails.AssetIndex = GameVersionDetails.AssetIndex(
    id = id,
    sha1 = sha1,
    size = size,
    totalSize = totalSize,
    url = url
)

private fun MojangArgument.convert(): GameVersionDetails.Argument =
    when (this) {
        is BasicArgument -> GameVersionDetails.BasicArgument(value)
        is ConditionalArgument -> GameVersionDetails.ConditionalArgument(rules.map { it.convert() }, value)
    }

private fun MojangVersionInfo.Rule.convert(): GameVersionDetails.Rule =
    GameVersionDetails.Rule(
        action = convertAction(),
        features = features,
        os = os?.let { os ->
            GameVersionDetails.Rule.Os(
                name = os.name,
                arch = os.arch,
                version = os.version
            )
        }
    )

private fun MojangVersionInfo.Rule.convertAction(): GameVersionDetails.Rule.Action = when (action) {
    "allow" -> GameVersionDetails.Rule.Action.Allow
    "deny" -> GameVersionDetails.Rule.Action.Deny
    else -> error("Unknown action $this")
}