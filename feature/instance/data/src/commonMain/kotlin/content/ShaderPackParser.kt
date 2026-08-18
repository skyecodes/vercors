package app.vercors.feature.instance.data.content

import kotlinx.io.files.Path

/**
 * Shader packs (Iris/OptiFine convention) have no standardized manifest — just a `shaders/` directory of
 * `.fsh`/`.vsh` files, sometimes a `shaders/shaders.properties` (a renderer config file, not metadata). There's
 * no title/description/version/icon convention to parse, so this only derives a filename-based title, matching
 * the mod/resource-pack parsers' own fallback behavior for files with nothing else to go on.
 */
internal object ShaderPackParser : ContentFileParser {
    override suspend fun parse(filePath: Path): ParsedContentInfo {
        val title = contentBaseName(filePath.name, "zip").replace(Regex("[-_]"), " ").trim()
        return ParsedContentInfo(title = title, description = null, version = null, loader = null, iconEntryPath = null)
    }
}
