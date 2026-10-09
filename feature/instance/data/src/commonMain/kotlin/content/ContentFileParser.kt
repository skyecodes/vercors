package app.vercors.feature.instance.data.content

import kotlinx.io.files.Path

internal fun interface ContentFileParser {
    suspend fun parse(filePath: Path): ParsedContentInfo
}
