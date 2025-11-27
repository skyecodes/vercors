package app.vercors.launcher.common.data.instance

import app.vercors.launcher.common.data.loader.ModLoaderTypeData
import app.vercors.launcher.common.data.loader.toData
import app.vercors.launcher.common.data.loader.toDomain
import app.vercors.launcher.common.domain.instance.model.InstanceModLoader
import kotlinx.serialization.Serializable

@Serializable
data class InstanceModLoaderData(
    val type: ModLoaderTypeData,
    val version: String,
)

fun InstanceModLoaderData.toDomain(): InstanceModLoader = InstanceModLoader(
    type = type.toDomain(),
    version = version,
)

fun InstanceModLoader.toData(): InstanceModLoaderData = InstanceModLoaderData(
    type = type.toData(),
    version = version,
)