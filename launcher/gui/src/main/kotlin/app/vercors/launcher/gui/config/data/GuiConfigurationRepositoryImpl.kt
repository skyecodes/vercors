package app.vercors.launcher.gui.config.data

import app.vercors.launcher.common.data.config.AbstractConfigurationRepositoryImpl
import app.vercors.launcher.gui.config.GuiConfigurationRepository
import app.vercors.launcher.gui.config.model.GuiConfiguration
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Single

@Single
internal class GuiConfigurationRepositoryImpl(
    coroutineScope: CoroutineScope,
    dataSource: GuiConfigurationDataSource
) : GuiConfigurationRepository, AbstractConfigurationRepositoryImpl<GuiConfiguration, GuiConfigurationData>(
    coroutineScope = coroutineScope,
    dataSource = dataSource,
    toDomain = GuiConfigurationData::toDomain,
    toData = GuiConfiguration::toData,
    default = ::GuiConfiguration
)