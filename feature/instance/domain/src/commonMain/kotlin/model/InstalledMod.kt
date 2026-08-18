package app.vercors.feature.instance.domain.model

import app.vercors.feature.game.domain.model.ModLoaderType

data class InstalledMod(
    val fileName: String,
    val title: String,
    val description: String?,
    val version: String,
    val loader: ModLoaderType?,
    val fileSize: Long,
    val iconFilePath: String?,
    val providers: List<ModProvider>,
    val enabled: Boolean,
)
