package app.vercors.feature.launcher.data.service

import app.vercors.core.system.SystemService
import app.vercors.feature.game.domain.model.GameAssetIndex
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.shared.data.AppPaths
import dev.zacsweers.metro.Inject

private const val GAME_ASSET_DOWNLOAD_URL = "https://resources.download.minecraft.net"

/** Maps a resolved version manifest onto the set of files an instance needs on disk. */
@Inject
class LaunchDependencyResolver(
    private val appPaths: AppPaths,
    private val systemService: SystemService
) {
    fun resolve(
        instanceId: InstanceId,
        details: GameVersionDetails,
        assetIndex: GameAssetIndex
    ): InstanceDependencyList {
        val nativesDir = appPaths.nativesDir(instanceId)
        return InstanceDependencyList(
            client = DependencyFile.Basic(
                url = details.clientDownload.url,
                path = appPaths.versionJarFile(details.id),
                size = details.clientDownload.size,
                sha1 = details.clientDownload.sha1,
            ),
            libraries = details.libraries.mapNotNull { it.artifact }.map {
                DependencyFile.Basic(
                    url = it.url,
                    path = appPaths.libraryFile(it.path),
                    size = it.size,
                    sha1 = it.sha1,
                )
            },
            natives = details.libraries.mapNotNull { lib ->
                lib.classifiers?.get(lib.natives?.get(systemService.osType.id))?.let {
                    DependencyFile.Native(
                        url = it.url,
                        path = appPaths.libraryFile(it.path),
                        size = it.size,
                        sha1 = it.sha1,
                        extractDest = nativesDir,
                        extractExclude = lib.extractExclude
                    )
                }
            },
            assets = assetIndex.objects.values.map {
                DependencyFile.Basic(
                    url = "$GAME_ASSET_DOWNLOAD_URL/${it.hash.take(2)}/${it.hash}",
                    path = appPaths.assetObjectFile(it.hash),
                    size = it.size,
                    sha1 = it.hash,
                )
            },
            logging = details.clientLogging?.let { clientLogging ->
                DependencyFile.Basic(
                    url = clientLogging.file.url,
                    path = appPaths.loggingConfigFile(clientLogging.type.dirName, clientLogging.file.id),
                    size = clientLogging.file.size,
                    sha1 = clientLogging.file.sha1,
                )
            },
        )
    }

    private val GameVersionDetails.Logging.Type.dirName: String
        get() = when (this) {
            GameVersionDetails.Logging.Type.LOG4J2_XML -> "log4j2-xml"
        }
}
