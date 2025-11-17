package app.vercors.launcher.common.domain.config

import app.vercors.shared.domain.Resource
import kotlinx.coroutines.flow.StateFlow

interface CommonConfigurationRepository {
    val state: StateFlow<Resource<CommonConfiguration>>

    suspend fun load()
    suspend fun save(commonConfiguration: CommonConfiguration)
    suspend fun update(block: (CommonConfiguration) -> CommonConfiguration)
}