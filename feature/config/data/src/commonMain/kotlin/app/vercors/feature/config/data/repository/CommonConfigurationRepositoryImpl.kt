package app.vercors.feature.config.data.repository

import app.vercors.feature.config.data.datasource.CommonConfigurationDataSource
import app.vercors.feature.config.data.dto.CommonConfigurationData
import app.vercors.feature.config.data.dto.toData
import app.vercors.feature.config.data.dto.toDomain
import app.vercors.feature.config.domain.model.CommonConfiguration
import app.vercors.feature.config.domain.repository.CommonConfigurationRepository
import dev.zacsweers.metro.*
import kotlinx.coroutines.CoroutineScope

@SingleIn(AppScope::class)
@ContributesBinding(
    scope = AppScope::class,
    binding = binding<CommonConfigurationRepository>()
)
@Inject
internal class CommonConfigurationRepositoryImpl(
    coroutineScope: CoroutineScope,
    dataSource: CommonConfigurationDataSource
) : CommonConfigurationRepository, AbstractConfigurationRepositoryImpl<CommonConfiguration, CommonConfigurationData>(
    coroutineScope = coroutineScope,
    dataSource = dataSource,
    toDomain = CommonConfigurationData::toDomain,
    toData = CommonConfiguration::toData
)
