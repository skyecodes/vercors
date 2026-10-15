package app.vercors.feature.config.ui.general

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.domain.model.HomeCategoryConfig
import app.vercors.feature.config.domain.model.StartupScreen
import app.vercors.feature.project.domain.model.ProjectPlatform

@Immutable
data class GeneralConfigurationState(
    val preferredPlatform: String?,
    val platformList: List<ProjectPlatform>,
    val startupScreen: StartupScreen?,
    val checkForInstanceUpdates: Boolean?,
    val homeCategories: List<HomeCategoryConfig>,
)
