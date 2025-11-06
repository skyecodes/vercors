package app.vercors.launcher.common.data.settings

import app.vercors.launcher.common.domain.settings.GlobalSettings
import kotlinx.io.files.Path
import kotlinx.serialization.Serializable

@Serializable
data class GlobalSettingsData(
    val baseDir: String,
)

fun GlobalSettingsData.toDomain(): GlobalSettings = GlobalSettings(
    baseDir = Path(baseDir),
)

fun GlobalSettings.toData(): GlobalSettingsData = GlobalSettingsData(
    baseDir = baseDir.toString(),
)