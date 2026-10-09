package app.vercors.feature.instance.data.content

import kotlinx.io.files.Path

// Shader packs have no manifest, so only a file-name title is derived
internal object ShaderPackParser : ContentFileParser {
    override suspend fun parse(filePath: Path): ParsedContentInfo {
        val title = contentBaseName(filePath.name, "zip").replace(Regex("[-_]"), " ").trim()
        return ParsedContentInfo(title = title, description = null, version = null, loader = null, iconEntryPath = null)
    }
}
