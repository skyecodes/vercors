package app.vercors.feature.theme.domain.model

import app.vercors.core.domain.ThemeColor
import app.vercors.core.domain.ThemeColors

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

data class ThemeNamedColor(
    val id: String,
    val name: String,
    val color: ThemeColor.Argb,
)
