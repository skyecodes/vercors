package app.vercors.feature.config.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ConfigurationState(
    val selectedTheme: ThemeView?,
    val themeList: List<ThemeView>,
    val selectedAccent: ThemeNamedColorView?,
    val animations: Boolean?,
)

@Immutable
data class ThemeView(
    val id: String,
    val displayName: String,
    val accents: List<ThemeNamedColorView>,
    val defaultAccent: ThemeNamedColorView,
)

@Immutable
data class ThemeNamedColorView(
    val id: String,
    val name: String,
    val color: Color,
)
