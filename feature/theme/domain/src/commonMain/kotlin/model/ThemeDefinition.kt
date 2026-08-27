package app.vercors.feature.theme.domain.model

import kotlin.jvm.JvmInline

data class ThemeDefinition(
    val id: String,
    val displayName: String,
    val isDark: Boolean,
    val colors: ThemeColors,
    val accents: List<ThemeNamedColor>,
    val defaultAccent: String,
    val source: ThemeSource,
)

enum class ThemeSource {
    Builtin,
    Custom
}

data class ThemeColors(
    val background: ThemeColor,
    val backgroundAlt: ThemeColor,
    val onBackground: ThemeColor,
    val primary: ThemeColor,
    val onPrimary: ThemeColor,
    val error: ThemeColor,
    val onError: ThemeColor,
    val success: ThemeColor,
    val onSuccess: ThemeColor,
    val warn: ThemeColor,
    val surface: ThemeColor,
    val surface2: ThemeColor,
    val surface3: ThemeColor,
)

data class ThemeNamedColor(
    val id: String,
    val name: String,
    val color: ThemeColor.Argb,
)

sealed interface ThemeColor {
    @JvmInline
    value class Argb(val value: Long) : ThemeColor

    @JvmInline
    value class Named(val name: String) : ThemeColor
}
