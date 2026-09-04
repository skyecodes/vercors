package app.vercors.feature.instance.data.content

import app.vercors.feature.game.domain.model.ModLoaderType
import kotlinx.io.files.Path

internal data class ParsedContentInfo(
    val title: String,
    val description: String?,
    val version: String?,
    val loader: ModLoaderType?,
    val iconEntryPath: String?
)

internal fun interface ContentFileParser {
    suspend fun parse(filePath: Path): ParsedContentInfo
}

/**
 * Some pack authors write raw Minecraft formatting codes (`§` followed by one color/style character) directly
 * into a text field a parser extracts from the file itself (e.g. `pack.mcmeta`'s `description`), since that's
 * literally how Minecraft renders them in-game — strip them so they don't show up as literal garbage in the UI.
 */
private val MINECRAFT_FORMATTING_CODE = Regex("§.")

internal fun String.stripMinecraftFormatting(): String =
    replace(MINECRAFT_FORMATTING_CODE, "").replace("§", "").ifBlank { this }
