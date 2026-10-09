package app.vercors.shared.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class InvalidParameterData(val name: String, val value: String)
