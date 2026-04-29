package app.vercors.feature.theme.data.datasource

interface BuiltinThemeResourceLoader {
    suspend fun loadResourceFromPath(path: String): String?
}
