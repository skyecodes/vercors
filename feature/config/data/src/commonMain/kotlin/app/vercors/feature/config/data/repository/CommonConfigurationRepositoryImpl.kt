package app.vercors.feature.config.data.repository

import app.vercors.feature.config.data.datasource.CommonConfigurationDataSource
import app.vercors.feature.config.data.dto.CommonConfigurationData
import app.vercors.feature.config.data.dto.toData
import app.vercors.feature.config.data.dto.toDomain
import app.vercors.feature.config.domain.model.CommonConfiguration
import app.vercors.feature.config.domain.repository.CommonConfigurationRepository
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
