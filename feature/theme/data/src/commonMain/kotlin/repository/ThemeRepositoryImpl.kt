package app.vercors.feature.theme.data.repository

import app.vercors.core.domain.ProgressiveStateFlow
import app.vercors.core.domain.progressiveStateIn
import app.vercors.feature.theme.data.ThemeError
import app.vercors.feature.theme.data.datasource.BuiltinThemeDataSource
import app.vercors.feature.theme.data.datasource.CustomThemeDataSource
import app.vercors.feature.theme.data.toDomain
import app.vercors.feature.theme.domain.model.ThemeCatalog
import app.vercors.feature.theme.domain.model.ThemeDefinition
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
                recover<ThemeError, ThemeDefinition?>({
                    it.toDomain(source = ThemeSource.Builtin)
                }) { null }
            }
            .associateBy { it.id }
        emit(builtins)
    }

    override val catalog: ProgressiveStateFlow<Nothing, ThemeCatalog> =
        combine(customDataSource.flow, builtinMapFlow) { customOutcome, builtinMap ->
            customOutcome.map { fileMap ->
                val customMap = fileMap.values
                    .mapNotNull {
                        recover<ThemeError, ThemeDefinition?>({
                            it.getOrNull()?.toDomain(source = ThemeSource.Custom)
                        }) { null }
                    }
                    .filterNot { it.id in builtinMap.keys } // built-ins win
                    .associateBy { it.id }

                val merged = builtinMap + customMap
                ThemeCatalog(
                    themes = merged.values.sortedBy { it.displayName.lowercase() },
                    byId = merged,
                )
            }
        }.progressiveStateIn(coroutineScope)
}
