package app.vercors.feature.config.ui

import androidx.compose.runtime.Immutable
import app.vercors.feature.config.ui.about.AboutConfigurationEvent
import app.vercors.feature.config.ui.about.AboutConfigurationState
import app.vercors.feature.config.ui.advanced.AdvancedConfigurationEvent
import app.vercors.feature.config.ui.advanced.AdvancedConfigurationState
import app.vercors.feature.config.ui.appearance.AppearanceConfigurationEvent
import app.vercors.feature.config.ui.appearance.AppearanceConfigurationState
import app.vercors.feature.config.ui.general.GeneralConfigurationEvent
import app.vercors.feature.config.ui.general.GeneralConfigurationState
import app.vercors.feature.config.ui.java.JavaConfigurationEvent
import app.vercors.feature.config.ui.java.JavaConfigurationState
import app.vercors.feature.config.ui.nativelibraries.NativeLibraryConfigurationEvent
import app.vercors.feature.config.ui.nativelibraries.NativeLibraryConfigurationState
import app.vercors.feature.config.ui.storage.StorageConfigurationEvent
import app.vercors.feature.config.ui.storage.StorageConfigurationState
import app.vercors.shared.ui.Store

@Immutable
data class ConfigurationState(
    val selectedTab: ConfigurationTab,
    val horizontalTabs: Boolean?,
    val showNativeLibrariesTab: Boolean,
    val updateAvailable: Boolean,
    val generalContent: Store<GeneralConfigurationState, GeneralConfigurationEvent>,
    val advancedContent: Store<AdvancedConfigurationState, AdvancedConfigurationEvent>,
    val appearanceContent: Store<AppearanceConfigurationState, AppearanceConfigurationEvent>,
    val javaContent: Store<JavaConfigurationState, JavaConfigurationEvent>,
    val nativeLibrariesContent: Store<NativeLibraryConfigurationState, NativeLibraryConfigurationEvent>,
    val storageContent: Store<StorageConfigurationState, StorageConfigurationEvent>,
    val aboutContent: Store<AboutConfigurationState, AboutConfigurationEvent>,
)
