package app.vercors.feature.config.ui.appearance

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.vercors.feature.theme.domain.model.ThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeNamedColor

@Immutable
data class AppearanceConfigurationState(
    val selectedTheme: ThemeView?,
    val themeList: List<ThemeView>,
    val selectedAccent: ThemeNamedColorView?,
    val animations: Boolean?,
    val uiScalePercent: Int?,
    val horizontalTabs: Boolean?,
)

@Immutable
data class ThemeView(
    val id: String,
    val displayName: String,
    val isDark: Boolean,
    val accents: List<ThemeNamedColorView>,
    val defaultAccent: ThemeNamedColorView,
)

@Immutable
data class ThemeNamedColorView(
    val id: String,
    val name: String,
    val color: Color,
)

fun ThemeDefinition.toView(): ThemeView = ThemeView(
    id = id,
    displayName = displayName,
    isDark = isDark,
    accents = accents.map { it.toView() },
    defaultAccent = accents.first { it.id == defaultAccent }.toView()
)

fun ThemeNamedColor.toView(): ThemeNamedColorView = ThemeNamedColorView(
    id = id,
    name = name,
    color = Color(color.value)
)
