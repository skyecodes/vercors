package app.vercors.shared.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.io.files.Path

/**
 * Single source of truth for the launcher's on-disk layout.
 *
 * Note that [instanceStore] and [gameDirs] are unrelated despite both being named "instances":
 * the former holds the serialized instance metadata, the latter holds the working directory each
 * instance's game process actually runs in.
 */
@SingleIn(AppScope::class)
@Inject
class AppPaths(root: Path) {
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

    /** Scratch space for in-progress downloads — deliberately outside any directory a content directory watcher observes. */
    val downloadsTmp = Path(data, "tmp")

    fun versionDir(versionId: String): Path = Path(versions, versionId)

    fun versionJarFile(versionId: String): Path = Path(versionDir(versionId), "$versionId.jar")

    fun versionInfoFile(versionId: String): Path = Path(versionDir(versionId), "$versionId.json")

    /** Where the list of mod loaders available for a given game version is cached. */
    fun availableLoadersFile(gameVersion: String): Path = Path(versionDir(gameVersion), "loaders.json")

    /** Where the list of available versions for a given mod loader on a given game version is cached. */
    fun loaderVersionsFile(gameVersion: String, loaderType: String): Path =
        Path(versionDir(gameVersion), "$loaderType-loader-versions.json")

    /** Where a mod loader's resolved profile (mainClass/libraries/args) is cached for a given game+loader version. */
    fun loaderProfileFile(gameVersion: String, loaderType: String, loaderVersion: String): Path =
        Path(versionDir("$gameVersion-$loaderType-$loaderVersion"), "profile.json")

    /** Where a Forge/NeoForge-family installer jar is cached for a given loader+version. */
    fun installerJarFile(loaderType: String, gameVersion: String, loaderVersion: String): Path =
        Path(versionDir("$gameVersion-$loaderType-$loaderVersion"), "installer.jar")

    fun assetIndexFile(assetIndexId: String): Path = Path(assetIndexes, "$assetIndexId.json")

    fun assetObjectFile(hash: String): Path = Path(assetObjects, hash.take(2), hash)

    /** Resolves a Mojang library artifact path such as `com/mojang/logging/1.0.0/logging-1.0.0.jar`. */
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

    /** Where a downloaded mod/resource pack/shader pack is cached, keyed by content kind + sha1, so identical
     *  files can be hardlinked into every instance that needs them instead of re-downloaded/re-stored per instance. */
    fun contentCacheObjectFile(kind: ContentCacheKind, sha1: String): Path =
        Path(contentCacheRoot, kind.name.lowercase(), sha1.take(2), sha1)

    fun instanceIconFile(instanceId: String, extension: String): Path = Path(gameDir(instanceId), "icon.$extension")

    fun javaRuntimeDir(component: String): Path = Path(data, "java", "runtimes", component)

    private fun String.pathSegments(): Array<String> = split('/').filter { it.isNotBlank() }.toTypedArray()
}
