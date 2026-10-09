package app.vercors.feature.instance.data.content

import app.vercors.feature.game.domain.model.ModLoaderType

internal data class ParsedContentInfo(
    val title: String,
    val description: String?,
    val version: String?,
    val loader: ModLoaderType?,
    val iconEntryPath: String?
)
