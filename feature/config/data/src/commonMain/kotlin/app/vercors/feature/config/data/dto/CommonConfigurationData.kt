package app.vercors.feature.config.data.dto

import app.vercors.feature.config.domain.model.CommonConfiguration
import kotlinx.serialization.Serializable

@Serializable
internal data class CommonConfigurationData(
    val testConfig: String
)

internal fun CommonConfigurationData.toDomain(): CommonConfiguration = CommonConfiguration(
    testConfig = testConfig,
)

internal fun CommonConfiguration.toData(): CommonConfigurationData = CommonConfigurationData(
    testConfig = testConfig,
)
