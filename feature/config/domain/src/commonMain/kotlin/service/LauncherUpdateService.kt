package app.vercors.feature.config.domain.service

import app.vercors.feature.config.domain.model.LauncherUpdateState
import app.vercors.feature.config.domain.model.UpdateChannel
import kotlinx.coroutines.flow.StateFlow

interface LauncherUpdateService {
    val state: StateFlow<LauncherUpdateState>
    suspend fun check(channel: UpdateChannel)
    suspend fun download()
    fun installAndRestart()
}
