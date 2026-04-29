package app.vercors.feature.theme.domain.model

data class ThemeCatalog(
    val themes: List<AppThemeDefinition>,
    val byId: Map<String, AppThemeDefinition>,
)
