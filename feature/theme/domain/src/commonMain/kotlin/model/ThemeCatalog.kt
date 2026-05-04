package app.vercors.feature.theme.domain.model

data class ThemeCatalog(
    val themes: List<ThemeDefinition>,
    val byId: Map<String, ThemeDefinition>,
)
