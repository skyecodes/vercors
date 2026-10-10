package app.vercors.launcher.gui

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.theme.domain.model.ThemeCatalog
import app.vercors.feature.theme.domain.model.ThemeDefinition

fun ThemeCatalog.selectedTheme(configuration: Configuration): ThemeDefinition? =
    byId[configuration.selectedThemeId] ?: byId[Configuration.DEFAULT_THEME_ID]

fun ThemeDefinition.selectedAccentColor(configuration: Configuration): Long? =
    accents.find { it.id == configuration.selectedAccent }?.color?.value
