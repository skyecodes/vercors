package app.vercors.feature.config.domain.repository

import app.vercors.core.domain.Resource
import kotlinx.coroutines.flow.StateFlow

interface AbstractConfigurationRepository<T : Any> {
    val state: StateFlow<Resource<T>>

    suspend fun load()
    suspend fun save(commonConfiguration: T)
    suspend fun update(block: (T) -> T)
}
