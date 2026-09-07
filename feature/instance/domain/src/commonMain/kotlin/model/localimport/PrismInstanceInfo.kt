package app.vercors.feature.instance.domain.model.localimport

import app.vercors.feature.game.domain.model.ModLoaderType

data class PrismInstanceInfo(
    val name: String,
    val gameVersion: String,
    val loaderType: ModLoaderType?,
    val loaderVersion: String?,
    val rootPrefix: String,
    val iconEntryPath: String?,
)
