package app.vercors.meta.backend.game

import app.vercors.meta.backend.game.mojang.dto.*
import app.vercors.meta.lib.game.BriefGameVersionData
import app.vercors.meta.lib.game.GameVersionDetailsData
import app.vercors.meta.lib.game.GameVersionListData
import app.vercors.meta.lib.game.GameVersionTypeData
import kotlin.time.toKotlinInstant

internal fun MojangVersionManifest.convert(): GameVersionListData = GameVersionListData(
    latestRelease = latest.release,
    latestSnapshot = latest.snapshot,
    versions = versions.sortedBy { it.releaseTime }.reversed().map {
        BriefGameVersionData(
            id = it.id,
            type = it.type.convert(),
        )
    }
)

private fun MojangReleaseType.convert(): GameVersionTypeData = when (this) {
    MojangReleaseType.Release -> GameVersionTypeData.Release
    MojangReleaseType.Snapshot -> GameVersionTypeData.Snapshot
    MojangReleaseType.Beta -> GameVersionTypeData.Beta
    MojangReleaseType.Alpha -> GameVersionTypeData.Alpha
}

internal fun MojangVersionInfo.convert(assetIndexData: MojangAssetIndex): GameVersionDetailsData =
    GameVersionDetailsData(
    id = id,
    gameArgs = arguments?.game?.map { it.convert() },
    jvmArgs = arguments?.jvm?.map { it.convert() },
    clientDownload = downloads.client.convert(),
    serverDownload = downloads.server?.convert(),
    javaVersion = javaVersion?.convert(),
    libraries = libraries.map { it.convert() },
    clientLogging = logging?.client?.convert(),
    mainClass = mainClass,
    minimumLauncherVersion = minimumLauncherVersion,
    releaseTime = releaseTime.toKotlinInstant(),
        type = type.convert(),
        assetsId = assets,
        assets = assetIndexData.objects.map { it.value.convert() }
)

private fun MojangVersionInfo.Logging.File.convert(): GameVersionDetailsData.Logging.File =
    GameVersionDetailsData.Logging.File(
    id = id,
    sha1 = sha1,
    size = size,
    url = url,
)

private fun MojangVersionInfo.Logging.Type.convert(): GameVersionDetailsData.Logging.Type = when (this) {
    MojangVersionInfo.Logging.Type.Log4j2Xml -> GameVersionDetailsData.Logging.Type.LOG4J2_XML
}

private fun MojangVersionInfo.Logging.Client.convert(): GameVersionDetailsData.Logging = GameVersionDetailsData.Logging(
    argument = argument,
    file = file.convert(),
    type = type.convert()
)

private fun MojangVersionInfo.Library.convert(): GameVersionDetailsData.Library =
    GameVersionDetailsData.Library(
        name = name,
        artifact = downloads.artifact?.convert(),
        classifiers = downloads.classifiers?.mapValues { (_, value) -> value.convert() },
        natives = natives,
        extractExclude = extract?.exclude,
        rules = rules?.map { it.convert() }
    )

private fun MojangVersionInfo.Library.Downloads.Artifact.convert(): GameVersionDetailsData.Library.Artifact =
    GameVersionDetailsData.Library.Artifact(
        path = path,
        sha1 = sha1,
        size = size,
        url = url
    )

private fun MojangVersionInfo.JavaVersion.convert(): GameVersionDetailsData.JavaVersion =
    GameVersionDetailsData.JavaVersion(
    component = component,
    majorVersion = majorVersion
)

private fun MojangVersionInfo.Downloads.Download.convert(): GameVersionDetailsData.Download =
    GameVersionDetailsData.Download(
    sha1 = sha1,
    size = size,
    url = url
)

private fun MojangArgument.convert(): GameVersionDetailsData.Argument =
    when (this) {
        is BasicArgument -> GameVersionDetailsData.Argument.Basic(value)
        is ConditionalArgument -> GameVersionDetailsData.Argument.Conditional(rules.map { it.convert() }, value)
    }

private fun MojangVersionInfo.Rule.convert(): GameVersionDetailsData.Rule =
    GameVersionDetailsData.Rule(
        action = action.convert(),
        features = features,
        os = os?.let { os ->
            GameVersionDetailsData.Rule.Os(
                name = os.name,
                arch = os.arch,
                version = os.version
            )
        }
    )

private fun MojangVersionInfo.Rule.Action.convert(): GameVersionDetailsData.Rule.Action = when (this) {
    MojangVersionInfo.Rule.Action.Allow -> GameVersionDetailsData.Rule.Action.Allow
    MojangVersionInfo.Rule.Action.Disallow -> GameVersionDetailsData.Rule.Action.Disallow
}

private fun MojangAssetIndex.Asset.convert(): GameVersionDetailsData.Asset = GameVersionDetailsData.Asset(
    hash = hash,
    size = size
)
