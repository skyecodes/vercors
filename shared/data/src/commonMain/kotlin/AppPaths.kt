package app.vercors.shared.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path

// instanceStore holds instance metadata; gameDirs holds each instance's game working directory
@SingleIn(AppScope::class)
@Inject
class AppPaths(val root: Path) {
    val accountsFile = Path(root, "accounts.json")
    val configFile = Path(root, "config.json")
    val privateKeyFile = Path(root, "key.priv")
    val instanceStore = Path(root, "instances")
    val themes = Path(root, "themes")
    val logs = Path(root, "logs")

    val data = Path(root, "data")
    val versions = Path(data, "versions")
    val versionManifestFile = Path(versions, "manifest.json")
    val libraries = Path(data, "libraries")
    val assets = Path(data, "assets")
    val assetIndexes = Path(assets, "indexes")
    val assetObjects = Path(assets, "objects")
    val loggingConfigs = Path(data, "logging")
    val gameDirs = Path(data, "instances")
    val javaRuntimeManifestFile = Path(data, "java", "runtime-manifest.json")
    val imageCache = Path(data, "cache", "image")
    val metadataCacheRoot = Path(data, "cache", "metadata")
    val iconCacheRoot = Path(data, "cache", "icon")
    val contentCacheRoot = Path(data, "cache", "projects")

    // Outside any directory a content watcher observes
    val downloadsTmp = Path(data, "tmp")

    fun versionDir(versionId: String): Path = Path(versions, versionId)

    fun versionJarFile(versionId: String): Path = Path(versionDir(versionId), "$versionId.jar")

    fun versionInfoFile(versionId: String): Path = Path(versionDir(versionId), "$versionId.json")

    fun availableLoadersFile(gameVersion: String): Path = Path(versionDir(gameVersion), "loaders.json")

    fun loaderVersionsFile(gameVersion: String, loaderType: String): Path =
        Path(versionDir(gameVersion), "$loaderType-loader-versions.json")

    fun loaderProfileFile(gameVersion: String, loaderType: String, loaderVersion: String): Path =
        Path(versionDir("$gameVersion-$loaderType-$loaderVersion"), "profile.json")

    fun installerJarFile(loaderType: String, gameVersion: String, loaderVersion: String): Path =
        Path(versionDir("$gameVersion-$loaderType-$loaderVersion"), "installer.jar")

    fun assetIndexFile(assetIndexId: String): Path = Path(assetIndexes, "$assetIndexId.json")

    fun assetObjectFile(hash: String): Path = Path(assetObjects, hash.take(2), hash)

    fun libraryFile(artifactPath: String): Path = Path(libraries, *artifactPath.pathSegments())

    fun loggingConfigFile(typeDir: String, fileId: String): Path = Path(loggingConfigs, typeDir, fileId)

    fun gameDir(instanceId: String): Path = Path(gameDirs, instanceId)

    fun nativesDir(instanceId: String): Path = Path(gameDir(instanceId), "natives")

    fun modsDir(instanceId: String): Path = Path(gameDir(instanceId), "mods")

    fun resourcePacksDir(instanceId: String): Path = Path(gameDir(instanceId), "resourcepacks")

    fun shaderPacksDir(instanceId: String): Path = Path(gameDir(instanceId), "shaderpacks")

    fun configDir(instanceId: String): Path = Path(gameDir(instanceId), "config")

    fun savesDir(instanceId: String): Path = Path(gameDir(instanceId), "saves")

    fun worldDir(instanceId: String, worldName: String): Path = Path(savesDir(instanceId), worldName)

    fun serversDatFile(instanceId: String): Path = Path(gameDir(instanceId), "servers.dat")

    fun metadataCache(kind: String): Path = Path(metadataCacheRoot, kind)

    fun iconCache(kind: String): Path = Path(iconCacheRoot, kind)

    fun contentCacheObjectFile(kind: ContentCacheKind, sha1: String): Path =
        Path(contentCacheRoot, kind.name.lowercase(), sha1.take(2), sha1)

    fun instanceIconFile(instanceId: String, extension: String): Path = Path(gameDir(instanceId), "icon.$extension")

    fun javaRuntimeDir(component: String): Path = Path(data, "java", "runtimes", component)

    private fun String.pathSegments(): Array<String> = split('/').filter { it.isNotBlank() }.toTypedArray()
}
