package app.vercors.feature.instance.data.repository

import app.vercors.core.filesystem.*
import app.vercors.feature.instance.data.dto.DefaultOptionsData
import app.vercors.feature.instance.data.dto.toData
import app.vercors.feature.instance.data.dto.toDomain
import app.vercors.feature.instance.domain.model.options.*
import app.vercors.feature.instance.domain.repository.InstanceOptionsRepository
import app.vercors.shared.data.AppPaths
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private val logger = KotlinLogging.logger {}

private const val OPTIONS_FILE = "options.txt"
private const val TEMPLATE_FILE = "template.json"
private const val RESOURCE_PACKS_DIR = "resourcepacks"
private const val FILE_PACK_PREFIX = "file/"
private const val TMP_SUFFIX = ".tmp"
private val RESOURCE_PACK_LIST_KEYS = listOf("resourcePacks", "incompatibleResourcePacks")

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class InstanceOptionsRepositoryImpl(
    private val appPaths: AppPaths,
    private val json: Json,
) : InstanceOptionsRepository {
    private val defaultOptions = MutableStateFlow<DefaultOptions?>(null)
    private val defaultOptionsMutex = Mutex()
    private var defaultOptionsLoaded = false

    private val templateFile get() = Path(appPaths.defaultOptions, TEMPLATE_FILE)

    override fun observeDefaultOptions(): Flow<DefaultOptions?> = flow {
        loadDefaultOptions()
        emitAll(defaultOptions)
    }

    override suspend fun getDefaultOptions(): DefaultOptions? {
        loadDefaultOptions()
        return defaultOptions.value
    }

    context(_: Raise<InstanceOptionsError>)
    override suspend fun readOptions(source: OptionsSource): GameOptions? {
        val file = Path(source.dir, OPTIONS_FILE)
        if (!file.exists()) return null
        return orRaise(InstanceOptionsError.ReadFailed) {
            parseGameOptions(withContext(Dispatchers.IO) { file.source { it.readString() } })
        }
    }

    context(_: Raise<InstanceOptionsError>)
    override suspend fun writeOptions(target: OptionsSource, options: GameOptions) {
        val file = Path(target.dir, OPTIONS_FILE)
        orRaise(InstanceOptionsError.WriteFailed) {
            val tmp = Path("$file$TMP_SUFFIX")
            tmp.writeText(options.format())
            tmp.moveTo(file)
        }
    }

    context(_: Raise<InstanceOptionsError>)
    override suspend fun copyFile(from: OptionsSource, to: OptionsSource, file: OptionsFile, overwrite: Boolean) {
        val source = Path(from.dir, file.fileName)
        val target = Path(to.dir, file.fileName)
        if (!source.exists() || (!overwrite && target.exists())) return
        orRaise(InstanceOptionsError.WriteFailed) {
            val tmp = Path("$target$TMP_SUFFIX")
            source.copyTo(tmp)
            tmp.moveTo(target)
        }
    }

    context(_: Raise<InstanceOptionsError>)
    override suspend fun copyResourcePacks(from: OptionsSource, to: OptionsSource, options: GameOptions) {
        val sourceDir = Path(from.dir, RESOURCE_PACKS_DIR)
        val targetDir = Path(to.dir, RESOURCE_PACKS_DIR)
        for (name in options.resourcePackFileNames()) {
            val source = Path(sourceDir, name)
            val target = Path(targetDir, name)
            if (!source.exists() || target.exists()) continue
            orRaise(InstanceOptionsError.WriteFailed) {
                targetDir.createDirectories()
                // Folder packs are often edited in place, so they stay real copies instead of hardlinks
                if (source.isDirectory()) source.copyRecursively(target) else source.linkOrCopyTo(target)
            }
        }
    }

    context(_: Raise<InstanceOptionsError>)
    override suspend fun saveDefaultOptions(defaultOptions: DefaultOptions) {
        defaultOptionsMutex.withLock {
            orRaise(InstanceOptionsError.WriteFailed) { templateFile.writeJson(defaultOptions.toData(), json) }
            this.defaultOptions.value = defaultOptions
            defaultOptionsLoaded = true
        }
    }

    context(_: Raise<InstanceOptionsError>)
    override suspend fun clearDefaultOptions() {
        defaultOptionsMutex.withLock {
            val dir = appPaths.defaultOptions
            orRaise(InstanceOptionsError.WriteFailed) { if (dir.exists()) dir.deleteRecursively() }
            defaultOptions.value = null
            defaultOptionsLoaded = true
        }
    }

    private suspend fun loadDefaultOptions() = defaultOptionsMutex.withLock {
        if (defaultOptionsLoaded) return@withLock
        val file = templateFile
        defaultOptions.value = try {
            if (file.exists()) file.readJson<DefaultOptionsData>(json).toDomain() else null
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.warn(e) { "Could not read default options" }
            null
        }
        defaultOptionsLoaded = true
    }

    private val OptionsSource.dir: Path
        get() = when (this) {
            is OptionsSource.Instance -> appPaths.gameDir(instanceId)
            OptionsSource.Default -> appPaths.defaultOptions
        }

    private fun GameOptions.resourcePackFileNames(): Set<String> = RESOURCE_PACK_LIST_KEYS
        .mapNotNull { values[it] }
        .flatMap { value ->
            val list = runCatching { json.parseToJsonElement(value) as? JsonArray }.getOrNull().orEmpty()
            list.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        }
        .filter { it.startsWith(FILE_PACK_PREFIX) }
        .map { it.removePrefix(FILE_PACK_PREFIX) }
        .filter { it.isNotEmpty() && '/' !in it && '\\' !in it && it != ".." }
        .toSet()
}

private val OptionsFile.fileName: String
    get() = when (this) {
        OptionsFile.ServersDat -> "servers.dat"
        OptionsFile.HotbarNbt -> "hotbar.nbt"
    }

context(_: Raise<InstanceOptionsError>)
private suspend inline fun <T> orRaise(error: InstanceOptionsError, block: () -> T): T = try {
    block()
} catch (e: Exception) {
    currentCoroutineContext().ensureActive()
    logger.warn(e) { "Options file operation failed" }
    raise(error)
}
