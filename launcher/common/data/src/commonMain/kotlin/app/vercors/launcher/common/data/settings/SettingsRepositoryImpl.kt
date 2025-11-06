package app.vercors.launcher.common.data.settings

import app.vercors.launcher.common.data.IODispatcher
import app.vercors.launcher.common.data.deleteRecursively
import app.vercors.launcher.common.domain.settings.GlobalSettings
import app.vercors.launcher.common.domain.settings.SettingsRepository
import app.vercors.shared.domain.*
import ca.gosyer.appdirs.AppDirs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import org.koin.core.annotation.Single

internal const val GLOBAL_SETTINGS_FILE_NAME = "global_settings.json"

@Single
class SettingsRepositoryImpl(
    private val appDirs: AppDirs,
    private val json: Json,
    @param:IODispatcher private val ioDispatcher: CoroutineDispatcher,
    coroutineScope: CoroutineScope,
) : SettingsRepository {
    private val _globalSettings: MutableResourceStateFlow<GlobalSettings, Throwable> = MutableResourceStateFlow()
    override val globalSettings: ResourceStateFlow<GlobalSettings, Throwable> =
        _globalSettings.asResourceStateFlow(coroutineScope, true, ::loadGlobalSettings)
    private val globalSettingsMutex = Mutex()

    private val globalSettingsFile = Path(appDirs.getUserConfigDir(), GLOBAL_SETTINGS_FILE_NAME)

    override suspend fun loadGlobalSettings() {
        if (SystemFileSystem.exists(globalSettingsFile)) {
            if (SystemFileSystem.metadataOrNull(globalSettingsFile)?.isRegularFile == true) {
                _globalSettings.load(globalSettingsMutex) {
                    withContext(ioDispatcher) {
                        SystemFileSystem.source(globalSettingsFile).buffered().use {
                            json.decodeFromSource<GlobalSettingsData>(it).toDomain()
                        }
                    }
                }
                return
            } else {
                SystemFileSystem.deleteRecursively(globalSettingsFile)
            }
        }
        updateGlobalSettings(
            GlobalSettings(
                baseDir = Path(appDirs.getUserDataDir())
            )
        )
    }

    override suspend fun updateGlobalSettings(settings: GlobalSettings) {
        if (SystemFileSystem.exists(globalSettingsFile) && SystemFileSystem.metadataOrNull(globalSettingsFile)?.isRegularFile != true) {
            SystemFileSystem.deleteRecursively(globalSettingsFile)
        }
        SystemFileSystem.createDirectories(globalSettingsFile.parent!!)
        withContext(ioDispatcher) {
            SystemFileSystem.sink(globalSettingsFile).buffered().use {
                json.encodeToSink(settings.toData(), it)
            }
        }
        _globalSettings.updateMap { settings }
    }

}