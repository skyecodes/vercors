package app.vercors.launcher.common.data.config

import app.vercors.launcher.common.domain.config.model.CommonConfiguration
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