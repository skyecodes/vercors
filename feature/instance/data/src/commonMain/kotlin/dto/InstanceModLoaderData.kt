package app.vercors.feature.instance.data.dto

import app.vercors.feature.instance.domain.model.InstanceModLoader
import kotlinx.serialization.Serializable

@Serializable
data class InstanceModLoaderData(
    val type: InstanceModLoaderTypeData,
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
