package app.vercors.launcher.common.domain.settings

import app.vercors.shared.domain.ResourceStateFlow

interface SettingsRepository {
    val globalSettings: ResourceStateFlow<GlobalSettings, Throwable>

    suspend fun loadGlobalSettings()
    suspend fun updateGlobalSettings(settings: GlobalSettings)
}