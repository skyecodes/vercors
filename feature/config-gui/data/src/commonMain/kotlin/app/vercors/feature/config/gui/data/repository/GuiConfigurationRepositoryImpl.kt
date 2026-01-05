package app.vercors.feature.config.gui.data.repository

import app.vercors.feature.config.data.repository.AbstractConfigurationRepositoryImpl
import app.vercors.feature.config.gui.data.datasource.GuiConfigurationDataSource
import app.vercors.feature.config.gui.data.dto.GuiConfigurationData
import app.vercors.feature.config.gui.data.dto.toData
import app.vercors.feature.config.gui.data.dto.toDomain
import app.vercors.feature.config.gui.domain.model.GuiConfiguration
import app.vercors.feature.config.gui.domain.repository.GuiConfigurationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope

@SingleIn(AppScope::class)
@ContributesBinding(
    scope = AppScope::class,
    binding = binding<GuiConfigurationRepository>()
)
class GuiConfigurationRepositoryImpl(
    coroutineScope: CoroutineScope,
    dataSource: GuiConfigurationDataSource
) : GuiConfigurationRepository, AbstractConfigurationRepositoryImpl<GuiConfiguration, GuiConfigurationData>(
    coroutineScope = coroutineScope,
    dataSource = dataSource,
    toDomain = GuiConfigurationData::toDomain,
    toData = GuiConfiguration::toData
)
