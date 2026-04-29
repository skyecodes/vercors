package app.vercors.feature.theme.data.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.core.domain.progressiveStateIn
import app.vercors.feature.theme.data.builtin.BuiltInThemes
import app.vercors.feature.theme.data.datasource.BuiltinThemeDataSource
import app.vercors.feature.theme.data.datasource.CustomThemeDataSource
import app.vercors.feature.theme.data.mapper.ThemeParseError
import app.vercors.feature.theme.data.mapper.toDomain
import app.vercors.feature.theme.domain.model.AppThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeCatalog
import app.vercors.feature.theme.domain.model.ThemeSource
import app.vercors.feature.theme.domain.repository.ThemeRepository
import arrow.core.raise.recover
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import opensavvy.state.progressive.map
import opensavvy.state.progressive.valueOrNull

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ThemeRepositoryImpl(
    coroutineScope: CoroutineScope,
    customDataSource: CustomThemeDataSource,
    builtinDataSource: BuiltinThemeDataSource,
) : ThemeRepository {
    private val builtinMapFlow = flow {
        val builtins = builtinDataSource.loadAll()
            .mapNotNull {
                recover<ThemeParseError, AppThemeDefinition?>({
                    it.toDomain(source = ThemeSource.BuiltIn)
                }) { null }
            }
            .associateBy { it.id.value }
        emit(builtins)
    }

    override val catalog: ProgressiveStateFlow<Nothing, ThemeCatalog> =
        combine(customDataSource.flow, builtinMapFlow) { customOutcome, builtinMap ->
            customOutcome.map { fileMap ->
                val customMap = fileMap.values
                    .mapNotNull {
                        recover<ThemeParseError, AppThemeDefinition?>({
                            it.getOrNull()?.toDomain(source = ThemeSource.Custom)
                        }) { null }
                    }
                    .filterNot { it.id.value in builtinMap.keys } // built-ins win
                    .associateBy { it.id.value }

                val merged = builtinMap + customMap
                ThemeCatalog(
                    themes = merged.values.sortedBy { it.displayName.lowercase() },
                    byId = merged,
                )
            }
        }.progressiveStateIn(coroutineScope)

    override fun resolveTheme(selectedThemeId: String): AppThemeDefinition {
        val current = catalog.value.valueOrNull?.byId.orEmpty()
        return current[selectedThemeId] ?: BuiltInThemes.catppuccinMocha
    }
}
