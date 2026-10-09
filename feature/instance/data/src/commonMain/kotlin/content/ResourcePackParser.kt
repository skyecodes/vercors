package app.vercors.feature.instance.data.content

import app.vercors.feature.instance.data.dto.PackMcmeta
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import kotlinx.coroutines.CancellationException
import kotlinx.io.files.Path
import kotlinx.serialization.json.*

// pack.mcmeta has no title: Minecraft shows the file name, so this does too
internal class ResourcePackParser(private val json: Json) : ContentFileParser {
    override suspend fun parse(filePath: Path): ParsedContentInfo {
        val title = contentBaseName(filePath.name, "zip").replace(Regex("[-_]"), " ").trim()
        return runCatching {
            Zip.open(filePath).use { zip ->
                val entryPaths = buildSet { zip.forEachEntry { add(it.path.toString()) } }
                val description = if ("pack.mcmeta" in entryPaths) {
                    val text = zip.entry(Path("pack.mcmeta")) { readToBytes() }.decodeToString()
                    json.decodeFromString(PackMcmeta.serializer(), text).pack.description?.toDisplayText()
                        ?.stripMinecraftFormatting()
                } else null
                val iconEntryPath = "pack.png".takeIf { it in entryPaths }
                ParsedContentInfo(
                    title = title,
                    description = description,
                    version = null,
                    loader = null,
                    iconEntryPath = iconEntryPath
                )
            }
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            ParsedContentInfo(title = title, description = null, version = null, loader = null, iconEntryPath = null)
        }
    }
}

private fun JsonElement.toDisplayText(): String? = when (this) {
    is JsonPrimitive -> contentOrNull
    is JsonObject -> this["text"]?.jsonPrimitive?.contentOrNull
    is JsonArray -> mapNotNull { it.toDisplayText() }.joinToString("").takeIf { it.isNotEmpty() }
}
