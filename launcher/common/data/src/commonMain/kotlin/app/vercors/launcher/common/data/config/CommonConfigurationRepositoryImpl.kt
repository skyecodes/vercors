package app.vercors.launcher.common.data.config

import app.vercors.launcher.common.domain.config.CommonConfigurationRepository
import app.vercors.launcher.common.domain.config.model.CommonConfiguration
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Single

@Single
internal class CommonConfigurationRepositoryImpl(
    coroutineScope: CoroutineScope,
    dataSource: CommonConfigurationDataSource
) : CommonConfigurationRepository, AbstractConfigurationRepositoryImpl<CommonConfiguration, CommonConfigurationData>(
    coroutineScope = coroutineScope,
    dataSource = dataSource,
    toDomain = CommonConfigurationData::toDomain,
    toData = CommonConfiguration::toData,
    default = ::CommonConfiguration
)