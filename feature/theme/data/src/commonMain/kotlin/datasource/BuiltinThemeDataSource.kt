package app.vercors.feature.theme.data.datasource

import app.vercors.feature.theme.data.dto.ThemeDefinitionData
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private const val THEME_DIRECTORY = "themes"
private const val THEME_INDEX_FILE_NAME = "index.json"

@SingleIn(AppScope::class)
@Inject
class BuiltinThemeDataSource(
    private val loader: BuiltinThemeResourceLoader,
    private val json: Json,
) {
    suspend fun loadAll(): List<ThemeDefinitionData> {
        val indexText = loader.loadResourceFromPath("$THEME_DIRECTORY/$THEME_INDEX_FILE_NAME") ?: return emptyList()
        val index = runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), indexText)
        }.getOrElse { return emptyList() }

        return index.mapNotNull { fileName ->
            val text = loader.loadResourceFromPath("$THEME_DIRECTORY/$fileName") ?: return@mapNotNull null
            runCatching {
                json.decodeFromString(ThemeDefinitionData.serializer(), text)
            }.getOrNull()
        }
    }
}
