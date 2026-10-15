package app.vercors.feature.instance.data.repository

import app.vercors.core.cryptography.CryptographyService
import app.vercors.core.filesystem.*
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.data.*
import app.vercors.feature.instance.data.dto.MmcPack
import app.vercors.feature.instance.data.dto.MmcPackComponent
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.model.export.ExportableEntry
import app.vercors.feature.instance.domain.model.export.InstanceExportError
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.instance.domain.repository.InstanceExportRepository
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.curseForgeFingerprint
import app.vercors.shared.data.writeZipAtomically
import app.vercors.shared.domain.model.JvmMemoryOptions
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import opensavvy.progress.Progress
import opensavvy.progress.loading

private val ALWAYS_EXCLUDED_ENTRIES = setOf("natives", ".vercors")
private val EXCLUDED_BY_DEFAULT_ENTRIES =
    setOf("logs", "crash-reports", "screenshots", "downloads", ".cache", ".fabric", ".mixin.out")
private val HASHED_CONTENT_DIRS = setOf("mods", "resourcepacks", "shaderpacks")
private const val ICON_FILE_PREFIX = "icon."
private const val DISABLED_FILE_SUFFIX = ".disabled"
private const val PRISM_GAME_DIR = ".minecraft"
private const val PRISM_DEFAULT_ICON_KEY = "default"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceExportRepositoryImpl(
    private val appPaths: AppPaths,
    private val json: Json,
    private val cryptographyService: CryptographyService,
) : InstanceExportRepository {
    override suspend fun listExportableEntries(instanceId: InstanceId): List<ExportableEntry> {
        val gameDir = appPaths.gameDir(instanceId)
        if (!gameDir.exists()) return emptyList()
        return gameDir.list()
            .filter { it.name !in ALWAYS_EXCLUDED_ENTRIES && !it.name.startsWith(ICON_FILE_PREFIX) }
            .map { ExportableEntry(it.name, it.isDirectory(), it.name !in EXCLUDED_BY_DEFAULT_ENTRIES) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    context(_: Raise<InstanceExportError>)
    override suspend fun collectFiles(
        instanceId: InstanceId,
        entries: Set<String>,
        hashContent: Boolean,
        onProgress: (Progress) -> Unit
    ): List<InstanceExportFile> = orRaise(InstanceExportError.ReadFailed) {
        val gameDir = appPaths.gameDir(instanceId)
        val files = entries.filter { it !in ALWAYS_EXCLUDED_ENTRIES }.sorted()
            .flatMap { walkFiles(Path(gameDir, it), it) }
        files.mapIndexed { index, (path, relativePath) ->
            currentCoroutineContext().ensureActive()
            val hashed = hashContent && relativePath.isHashedContent()
            val sha1 = if (hashed) path.source { cryptographyService.sha1(it) }.toHexString() else null
            val fingerprint = if (hashed) path.curseForgeFingerprint() else null
            onProgress(loading((index + 1).toDouble() / files.size))
            InstanceExportFile(relativePath, path.toString(), path.metadataOrNull()?.size ?: 0, sha1, fingerprint)
        }
    }

    context(_: Raise<InstanceExportError>)
    override suspend fun writePrismExport(
        instance: Instance,
        files: List<InstanceExportFile>,
        targetPath: String,
        onProgress: (Progress) -> Unit
    ) {
        val iconPath = instance.iconFilePath?.let(::Path)?.takeIf { it.exists() }
        val iconKey = if (iconPath != null) "vercors_${instance.id}" else PRISM_DEFAULT_ICON_KEY
        val cfg = formatInstanceCfg(instance.toPrismCfg(iconKey))
        val mmcPack = json.encodeToString(MmcPack.serializer(), instance.toMmcPack())
        orRaise(InstanceExportError.WriteFailed) {
            Path(targetPath).writeZipAtomically {
                addBytes("instance.cfg", cfg.encodeToByteArray())
                addBytes("mmc-pack.json", mmcPack.encodeToByteArray())
                iconPath?.let { addFile("$iconKey.${it.name.substringAfterLast('.')}", it) }
                files.forEachIndexed { index, file ->
                    currentCoroutineContext().ensureActive()
                    addFile("$PRISM_GAME_DIR/${file.relativePath}", Path(file.path))
                    onProgress(loading((index + 1).toDouble() / files.size))
                }
            }
        }
    }

    private suspend fun walkFiles(path: Path, relativePath: String): List<Pair<Path, String>> = when {
        path.isDirectory() -> path.list().sortedBy { it.name }.flatMap { walkFiles(it, "$relativePath/${it.name}") }
        path.isRegularFile() -> listOf(path to relativePath)
        else -> emptyList()
    }
}

context(_: Raise<InstanceExportError>)
private suspend inline fun <T> orRaise(error: InstanceExportError, block: () -> T): T = try {
    block()
} catch (_: Exception) {
    currentCoroutineContext().ensureActive()
    raise(error)
}

// Disabled files keep the enabled file's hash, which would make a pack re-enable them on import
private fun String.isHashedContent(): Boolean {
    val segments = split('/')
    return segments.size == 2 && segments[0] in HASHED_CONTENT_DIRS && !segments[1].endsWith(DISABLED_FILE_SUFFIX)
}

private fun Instance.toPrismCfg(iconKey: String): Map<String, String> = buildMap {
    put("InstanceType", "OneSix")
    put("name", name)
    put("iconKey", iconKey)
    val memory = jvmMemoryOptions.withFallback(JvmMemoryOptions())
    val minMemoryMb = memory.minMemoryMb?.takeIf { memory.minMemoryEnabled == true }
    val maxMemoryMb = memory.maxMemoryMb?.takeIf { memory.maxMemoryEnabled == true }
    if (minMemoryMb != null || maxMemoryMb != null) {
        put("OverrideMemory", "true")
        minMemoryMb?.let { put("MinMemAlloc", it.toString()) }
        maxMemoryMb?.let { put("MaxMemAlloc", it.toString()) }
    }
    memory.extraArgs?.takeIf { it.isNotBlank() }?.let {
        put("OverrideJavaArgs", "true")
        put("JvmArgs", it)
    }
    modpackInfo?.let { info ->
        val packType = info.platformId.toManagedPackTypeOrNull() ?: return@let
        put("ManagedPack", "true")
        put("ManagedPackType", packType)
        put("ManagedPackID", info.projectId)
        put("ManagedPackVersionID", info.fileId)
        info.versionName?.let { put("ManagedPackVersionName", it) }
    }
}

private fun Instance.toMmcPack(): MmcPack = MmcPack(
    components = buildList {
        add(MmcPackComponent(PRISM_MINECRAFT_UID, gameVersion, important = true))
        modLoader?.let { loader ->
            if (loader.type == ModLoaderType.Fabric || loader.type == ModLoaderType.Quilt) {
                add(MmcPackComponent(PRISM_INTERMEDIARY_UID, gameVersion, dependencyOnly = true))
            }
            add(MmcPackComponent(loader.type.toPrismUid(), loader.version))
        }
    }
)
