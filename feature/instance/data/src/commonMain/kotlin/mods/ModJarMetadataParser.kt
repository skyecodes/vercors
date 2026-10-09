package app.vercors.feature.instance.data.mods

import app.vercors.core.filesystem.createParentDirectories
import app.vercors.core.filesystem.sink
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.data.content.contentBaseName
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal data class ParsedModInfo(
    val title: String,
    val description: String?,
    val version: String,
    val loader: ModLoaderType?,
    val iconEntryPath: String?
)

private const val MANIFEST_VERSION_PLACEHOLDER = $$"${file.jarVersion}"

// Real manifests carry many unmodelled fields, which Json and Toml reject by default
private val lenientToml = Toml(TomlInputConfig(ignoreUnknownNames = true))

private const val LEGACY_FORGE_METADATA_FILENAME = "mcmod.info"
private const val FABRIC_METADATA_FILENAME = "fabric.mod.json"
private const val QUILT_METADATA_FILENAME = "quilt.mod.json"
private const val FORGE_METADATA_FILENAME = "META-INF/mods.toml"
private const val NEOFORGE_METADATA_FILENAME = "META-INF/neoforge.mods.toml"
private const val MANIFEST_FILENAME = "META-INF/MANIFEST.MF"

internal fun parseModJar(jarPath: Path, json: Json): ParsedModInfo = runCatching {
    Zip.open(jarPath).use { zip ->
        val entryPaths = buildSet { zip.forEachEntry { add(it.path.toString()) } }
        when {
            FABRIC_METADATA_FILENAME in entryPaths -> zip.parseFabric(json)
            QUILT_METADATA_FILENAME in entryPaths -> zip.parseQuilt(json)
            FORGE_METADATA_FILENAME in entryPaths -> zip.parseForgeFamily(
                FORGE_METADATA_FILENAME,
                ModLoaderType.Forge,
                entryPaths
            )

            NEOFORGE_METADATA_FILENAME in entryPaths -> zip.parseForgeFamily(
                NEOFORGE_METADATA_FILENAME,
                ModLoaderType.NeoForge,
                entryPaths
            )

            LEGACY_FORGE_METADATA_FILENAME in entryPaths -> zip.parseLegacyForge(json)
            else -> null
        }
    }
}.getOrNull() ?: fallback(jarPath)

private fun Zip.parseFabric(json: Json): ParsedModInfo {
    val text = entry(Path(FABRIC_METADATA_FILENAME)) { readToBytes() }.decodeToString()
    val data = json.decodeFromString(FabricModJson.serializer(), text)
    return ParsedModInfo(
        title = data.name?.takeIf { it.isNotBlank() } ?: data.id,
        description = data.description?.takeIf { it.isNotBlank() },
        version = data.version,
        loader = ModLoaderType.Fabric,
        iconEntryPath = data.icon.iconPathOrNull()
    )
}

private fun Zip.parseQuilt(json: Json): ParsedModInfo {
    val text = entry(Path(QUILT_METADATA_FILENAME)) { readToBytes() }.decodeToString()
    val data = json.decodeFromString(QuiltModJson.serializer(), text)
    val metadata = data.quiltLoader.metadata
    return ParsedModInfo(
        title = metadata?.name?.takeIf { it.isNotBlank() } ?: data.quiltLoader.id,
        description = metadata?.description?.takeIf { it.isNotBlank() },
        version = data.quiltLoader.version,
        loader = ModLoaderType.Quilt,
        iconEntryPath = metadata?.icon.iconPathOrNull()
    )
}

private fun Zip.parseForgeFamily(
    entryName: String,
    loader: ModLoaderType,
    entryPaths: Set<String>
): ParsedModInfo {
    val text = entry(Path(entryName)) { readToBytes() }.decodeToString()
    val toml = lenientToml.decodeFromString(ForgeModsToml.serializer(), text)
    val mod = toml.mods.firstOrNull()
    // mods.toml often leaves version as "${file.jarVersion}": the real one is in the jar manifest
    val rawVersion = mod?.version?.takeIf { it.isNotBlank() && it != MANIFEST_VERSION_PLACEHOLDER }
    val version = rawVersion ?: readManifestImplementationVersion(entryPaths) ?: "unknown"
    return ParsedModInfo(
        title = mod?.displayName?.takeIf { it.isNotBlank() } ?: mod?.modId ?: entryName,
        description = mod?.description?.takeIf { it.isNotBlank() },
        version = version,
        loader = loader,
        iconEntryPath = mod?.logoFile ?: toml.logoFile
    )
}

private fun Zip.parseLegacyForge(json: Json): ParsedModInfo {
    val text = entry(Path(LEGACY_FORGE_METADATA_FILENAME)) { readToBytes() }.decodeToString()
    val mod = json.decodeFromString(ListSerializer(McModInfoEntry.serializer()), text).firstOrNull()
    return ParsedModInfo(
        title = mod?.name?.takeIf { it.isNotBlank() } ?: mod?.modid ?: LEGACY_FORGE_METADATA_FILENAME,
        description = mod?.description?.takeIf { it.isNotBlank() },
        version = mod?.version?.takeIf { it.isNotBlank() } ?: "unknown",
        loader = ModLoaderType.Forge,
        iconEntryPath = mod?.logoFile?.takeIf { it.isNotBlank() }
    )
}

private fun Zip.readManifestImplementationVersion(entryPaths: Set<String>): String? {
    if (MANIFEST_FILENAME !in entryPaths) return null
    val text = entry(Path(MANIFEST_FILENAME)) { readToBytes() }.decodeToString()
    return text.lineSequence()
        .firstOrNull { it.startsWith("Implementation-Version:") }
        ?.removePrefix("Implementation-Version:")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

internal suspend fun extractIcon(jarPath: Path, iconEntryPath: String, destDir: Path, destBaseName: String): String? =
    runCatching {
        Zip.open(jarPath).use { zip ->
            val normalized = iconEntryPath.removePrefix("/")
            val bytes = zip.entry(Path(normalized)) { readToBytes() }
            val fileName = "$destBaseName.${normalized.substringAfterLast('.', "png")}"
            val dest = Path(destDir, fileName)
            dest.createParentDirectories()
            dest.sink { it.write(bytes) }
            fileName
        }
    }.getOrElse { e ->
        if (e is CancellationException) throw e
        null
    }

private fun fallback(jarPath: Path): ParsedModInfo = ParsedModInfo(
    title = contentBaseName(jarPath.name, "jar").replace(Regex("[-_]"), " ").trim(),
    description = null,
    version = "unknown",
    loader = null,
    iconEntryPath = null
)
