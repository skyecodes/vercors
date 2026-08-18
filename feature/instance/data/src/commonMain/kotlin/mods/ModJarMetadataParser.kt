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
import kotlinx.serialization.json.Json

internal data class ParsedModInfo(
    val title: String,
    val description: String?,
    val version: String,
    val loader: ModLoaderType?,
    val iconEntryPath: String?
)

private const val MANIFEST_VERSION_PLACEHOLDER = "\${file.jarVersion}"

// Real fabric.mod.json/quilt.mod.json/mods.toml files carry many fields beyond what we model here
// (entrypoints, mixins, dependencies, ...) — both kotlinx.serialization's Json and ktoml's Toml reject
// unknown keys by default, which would otherwise make every real-world mod jar fail to parse.
private val lenientToml = Toml(TomlInputConfig(ignoreUnknownNames = true))

/**
 * Reads a mod jar's own metadata manifest (whichever of the four loader formats it ships) to extract
 * title/description/version/icon. Never throws — an unrecognized or unparseable jar still produces a
 * [ParsedModInfo] derived from its filename, so a jar is never silently dropped from the Mods tab.
 */
internal fun parseModJar(jarPath: Path, json: Json): ParsedModInfo = runCatching {
    Zip.open(jarPath).use { zip ->
        val entryPaths = buildSet { zip.forEachEntry { add(it.path.toString()) } }
        when {
            "fabric.mod.json" in entryPaths -> zip.parseFabric(json)
            "quilt.mod.json" in entryPaths -> zip.parseQuilt(json)
            "META-INF/mods.toml" in entryPaths ->
                zip.parseForgeFamily("META-INF/mods.toml", ModLoaderType.Forge, entryPaths)

            "META-INF/neoforge.mods.toml" in entryPaths ->
                zip.parseForgeFamily("META-INF/neoforge.mods.toml", ModLoaderType.NeoForge, entryPaths)

            else -> null
        }
    }
}.getOrElse { e ->
    if (e is CancellationException) throw e
    null
} ?: fallback(jarPath)

private fun Zip.parseFabric(json: Json): ParsedModInfo {
    val text = entry(Path("fabric.mod.json")) { readToBytes() }.decodeToString()
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
    val text = entry(Path("quilt.mod.json")) { readToBytes() }.decodeToString()
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
    // mods.toml commonly leaves version as the literal placeholder "${file.jarVersion}", meaning the real
    // version has to be read from the jar manifest instead (set by the build tool at build time).
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

private fun Zip.readManifestImplementationVersion(entryPaths: Set<String>): String? {
    if ("META-INF/MANIFEST.MF" !in entryPaths) return null
    val text = entry(Path("META-INF/MANIFEST.MF")) { readToBytes() }.decodeToString()
    return text.lineSequence()
        .firstOrNull { it.startsWith("Implementation-Version:") }
        ?.removePrefix("Implementation-Version:")
        ?.trim()
        ?.takeIf { it.isNotBlank() }
}

/** Extracts the icon referenced by [iconEntryPath] out of the jar and writes it to [destDir], returning its filename. */
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
