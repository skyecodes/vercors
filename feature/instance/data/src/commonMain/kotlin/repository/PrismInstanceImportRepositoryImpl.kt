package app.vercors.feature.instance.data.repository

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.data.dto.MmcPack
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.feature.instance.domain.model.localimport.PrismInstanceInfo
import app.vercors.feature.instance.domain.repository.PrismInstanceImportRepository
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.toZipEntryName
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.loading
import kotlin.uuid.Uuid

private val TOP_LEVEL_INSTANCE_CFG_REGEX = Regex("""^([^/]+)/instance\.cfg$""")
private val ICON_EXTENSIONS = listOf("png", "jpg", "jpeg", "svg", "ico")
private val GAME_DIR_CANDIDATES = listOf(".minecraft/", "minecraft/")

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PrismInstanceImportRepositoryImpl(
    private val appPaths: AppPaths,
    private val json: Json
) : PrismInstanceImportRepository {

    context(_: Raise<LocalImportError>)
    override suspend fun parse(sourcePath: String): PrismInstanceInfo {
        val zipPath = Path(sourcePath)
        return try {
            Zip.open(zipPath).use { zip ->
                val rootPrefix = detectRootPrefix(zip) ?: raise(LocalImportError.NotPrismFormat)
                val cfg = parseInstanceCfg(
                    zip.entry(Path("${rootPrefix}instance.cfg")) { readToBytes() }.decodeToString()
                )
                val name = cfg["name"] ?: raise(LocalImportError.ParseFailed)
                val mmcPack = json.decodeFromString(
                    MmcPack.serializer(),
                    zip.entry(Path("${rootPrefix}mmc-pack.json")) { readToBytes() }.decodeToString()
                )
                val components = mmcPack.components.associateBy { it.uid }
                val gameVersion = components["net.minecraft"]?.version ?: raise(LocalImportError.ParseFailed)
                val (loaderType, loaderVersion) = components.entries.firstNotNullOfOrNull { (uid, component) ->
                    uid.toModLoaderTypeOrNull()?.let { type -> component.version?.let { version -> type to version } }
                } ?: (null to null)
                val iconEntryPath = cfg["iconKey"]?.let { findIconEntryPath(zip, rootPrefix, it) }
                val modpackInfo = detectManagedPackInfo(cfg)
                PrismInstanceInfo(name, gameVersion, loaderType, loaderVersion, rootPrefix, iconEntryPath, modpackInfo)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(LocalImportError.ParseFailed)
        }
    }

    context(_: Raise<LocalImportError>)
    override suspend fun importGameFiles(
        sourcePath: String,
        instanceId: String,
        info: PrismInstanceInfo,
        onProgress: (Progress) -> Unit
    ): String? {
        val zipPath = Path(sourcePath)
        val gameDir = appPaths.gameDir(instanceId)
        return try {
            Zip.open(zipPath).use { zip ->
                val prefix = detectGameDirPrefix(zip, info.rootPrefix)
                var total = 0
                zip.forEachEntry { entry ->
                    if (!entry.isDirectory && entry.path.toZipEntryName().startsWith(prefix)) total++
                }
                var done = 0
                zip.forEachEntry { entry ->
                    if (entry.isDirectory) return@forEachEntry
                    val entryPath = entry.path.toZipEntryName()
                    if (!entryPath.startsWith(prefix)) return@forEachEntry
                    val target = Path(gameDir, *entryPath.removePrefix(prefix).pathSegments())
                    target.parent?.let { SystemFileSystem.createDirectories(it) }
                    entry.readToPath(target)
                    done++
                    onProgress(if (total > 0) loading(done.toDouble() / total) else loading())
                }
                info.iconEntryPath?.let { iconEntryPath -> extractIconOrNull(zip, iconEntryPath) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            raise(LocalImportError.ExtractionFailed)
        }
    }

    private fun detectGameDirPrefix(zip: Zip, rootPrefix: String): String {
        val fullPrefixes = GAME_DIR_CANDIDATES.map { "$rootPrefix$it" }
        val matched = BooleanArray(fullPrefixes.size)
        zip.forEachEntry { entry ->
            if (!entry.isDirectory) {
                val path = entry.path.toZipEntryName()
                for (i in fullPrefixes.indices) {
                    if (!matched[i] && path.startsWith(fullPrefixes[i])) matched[i] = true
                }
            }
        }
        return fullPrefixes[matched.indexOfFirst { it }.takeIf { it >= 0 } ?: 0]
    }

    private fun detectRootPrefix(zip: Zip): String? {
        if (zip.entryOrNull(Path("instance.cfg")) { true } == true) return ""
        var found: String? = null
        zip.forEachEntry { entry ->
            if (found == null && !entry.isDirectory) {
                TOP_LEVEL_INSTANCE_CFG_REGEX.matchEntire(entry.path.toZipEntryName())
                    ?.let { found = "${it.groupValues[1]}/" }
            }
        }
        return found
    }

    private fun findIconEntryPath(zip: Zip, rootPrefix: String, iconKey: String): String? =
        ICON_EXTENSIONS.firstNotNullOfOrNull { ext ->
            "$rootPrefix$iconKey.$ext".takeIf { zip.entryOrNull(Path(it)) { true } == true }
        }

    private fun extractIconOrNull(zip: Zip, iconEntryPath: String): String? {
        val targetPath =
            Path(appPaths.downloadsTmp, "prism-icon-${Uuid.random()}.${iconEntryPath.substringAfterLast('.', "png")}")
        return zip.entryOrNull(Path(iconEntryPath)) {
            SystemFileSystem.createDirectories(appPaths.downloadsTmp)
            readToPath(targetPath)
            targetPath.toString()
        }
    }
}

private fun parseInstanceCfg(text: String): Map<String, String> = text.lineSequence()
    .map { it.trim() }
    .filter { it.isNotBlank() && !it.startsWith("#") }
    .mapNotNull { line ->
        val idx = line.indexOf('=')
        if (idx < 0) null else line.substring(0, idx).trim() to line.substring(idx + 1).trim()
    }
    .toMap()

private fun String.toModLoaderTypeOrNull(): ModLoaderType? = when (this) {
    "net.minecraftforge" -> ModLoaderType.Forge
    "net.neoforged" -> ModLoaderType.NeoForge
    "net.fabricmc.fabric-loader" -> ModLoaderType.Fabric
    "org.quiltmc.quilt-loader" -> ModLoaderType.Quilt
    else -> null
}

private fun String.toManagedPackPlatformIdOrNull(): String? = when (this) {
    "flame" -> "curseforge"
    "modrinth" -> "modrinth"
    else -> null
}

private fun detectManagedPackInfo(cfg: Map<String, String>): InstanceModpackInfo? {
    if (!cfg["ManagedPack"].equals("true", ignoreCase = true)) return null
    val platformId = cfg["ManagedPackType"]?.toManagedPackPlatformIdOrNull() ?: return null
    val projectId = cfg["ManagedPackID"]?.takeIf { it.isNotBlank() } ?: return null
    val fileId = cfg["ManagedPackVersionID"]?.takeIf { it.isNotBlank() } ?: return null
    return InstanceModpackInfo(platformId, projectId, fileId)
}

private fun String.pathSegments(): Array<String> = split('/').filter { it.isNotBlank() }.toTypedArray()
