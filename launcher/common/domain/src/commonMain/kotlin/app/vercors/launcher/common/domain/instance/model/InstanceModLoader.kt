package app.vercors.launcher.common.domain.instance.model

import app.vercors.launcher.common.domain.loader.ModLoaderType

data class InstanceModLoader(
    val type: ModLoaderType,
    val version: String,
)