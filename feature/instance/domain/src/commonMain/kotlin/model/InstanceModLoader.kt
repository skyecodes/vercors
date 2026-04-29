package app.vercors.feature.instance.domain.model

import app.vercors.feature.game.domain.model.ModLoaderType

data class InstanceModLoader(
    val type: ModLoaderType,
    val version: String,
)
