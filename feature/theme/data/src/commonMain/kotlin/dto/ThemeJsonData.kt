package app.vercors.feature.theme.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ThemeJsonData(
    val id: String,
    val displayName: String,
    val colors: ThemeColorsJsonData,
)

@Serializable
data class ThemeColorsJsonData(
    val background: String,
    val backgroundAlt: String,
    val onBackground: String,
    val primary: String,
    val onPrimary: String,
    val error: String,
    val onError: String,
    val success: String,
    val onSuccess: String,
    val warn: String,
    val surface: String,
    val surface2: String,
)
