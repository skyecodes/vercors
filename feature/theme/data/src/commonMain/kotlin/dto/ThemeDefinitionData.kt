package app.vercors.feature.theme.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ThemeDefinitionData(
    val id: String,
    val displayName: String,
    val isDark: Boolean,
    val colors: ThemeColorsData,
    val accents: List<ThemeNamedColorData>,
    val defaultAccent: String,
)

@Serializable
data class ThemeColorsData(
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

@Serializable
data class ThemeNamedColorData(
    val id: String,
    val name: String,
    val color: String,
)
