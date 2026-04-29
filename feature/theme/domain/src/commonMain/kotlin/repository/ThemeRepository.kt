package app.vercors.feature.theme.domain.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.feature.theme.domain.model.AppThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeCatalog

interface ThemeRepository {
    val catalog: ProgressiveStateFlow<Nothing, ThemeCatalog>

    fun resolveTheme(selectedThemeId: String): AppThemeDefinition
}
