package app.vercors.feature.config.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.vercors.core.ui.layout.AppSidebarTabItem
import app.vercors.core.ui.layout.AppTabGroup
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.warn
import app.vercors.feature.config.ui.about.AboutConfiguration
import app.vercors.feature.config.ui.advanced.AdvancedConfiguration
import app.vercors.feature.config.ui.appearance.AppearanceConfiguration
import app.vercors.feature.config.ui.gameoptions.GameOptionsConfiguration
import app.vercors.feature.config.ui.general.GeneralConfiguration
import app.vercors.feature.config.ui.java.JavaConfiguration
import app.vercors.feature.config.ui.nativelibraries.NativeLibraryConfiguration
import app.vercors.feature.config.ui.storage.StorageConfiguration
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.advanced
import app.vercors.shared.ui.java
import com.composables.icons.lucide.*
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import org.jetbrains.compose.resources.stringResource

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Composable
fun Configuration(
    store: Store<ConfigurationState, ConfigurationEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    AppTabGroup(
        selectedTab = state.selectedTab,
        onSelectedTabChange = { eventSink(ConfigurationEvent.OnTabClick(it)) },
        tabs = ConfigurationTab.entries.filter { it != ConfigurationTab.NativeLibraries || state.showNativeLibrariesTab },
        modifier = modifier.fillMaxSize(),
        orientation = if (state.horizontalTabs == true) Orientation.Horizontal else Orientation.Vertical,
        tabTitle = { tab ->
            AppSidebarTabItem(
                title = tab.title,
                icon = tab.icon,
                badgeColor = Theme[colors][warn].takeIf { tab == ConfigurationTab.About && state.updateAvailable }
            )
        }
    ) { tab -> ConfigurationTabContent(tab, state) }
}

@Composable
private fun ConfigurationTabContent(tab: ConfigurationTab, state: ConfigurationState) {
    when (tab) {
        ConfigurationTab.General -> GeneralConfiguration(state.generalContent, state.horizontalTabs)
        ConfigurationTab.Advanced -> AdvancedConfiguration(state.advancedContent, state.horizontalTabs)
        ConfigurationTab.Appearance -> AppearanceConfiguration(state.appearanceContent, state.horizontalTabs)
        ConfigurationTab.Java -> JavaConfiguration(state.javaContent, state.horizontalTabs)
        ConfigurationTab.GameOptions -> GameOptionsConfiguration(state.gameOptionsContent, state.horizontalTabs)
        ConfigurationTab.NativeLibraries ->
            NativeLibraryConfiguration(state.nativeLibrariesContent, state.horizontalTabs)

        ConfigurationTab.Storage -> StorageConfiguration(state.storageContent, state.horizontalTabs)
        ConfigurationTab.About -> AboutConfiguration(state.aboutContent, state.horizontalTabs)
    }
}

private val ConfigurationTab.icon: ImageVector
    @Stable get() = when (this) {
        ConfigurationTab.General -> Lucide.Settings
        ConfigurationTab.Appearance -> Lucide.Eye
        ConfigurationTab.Java -> Lucide.Coffee
        ConfigurationTab.GameOptions -> Lucide.Keyboard
        ConfigurationTab.NativeLibraries -> Lucide.Package
        ConfigurationTab.Storage -> Lucide.HardDrive
        ConfigurationTab.Advanced -> Lucide.Wrench
        ConfigurationTab.About -> Lucide.Info
    }

private val ConfigurationTab.title: String
    @Composable get() = when (this) {
        ConfigurationTab.General -> stringResource(ConfigRes.string.general)
        ConfigurationTab.Appearance -> stringResource(ConfigRes.string.appearance)
        ConfigurationTab.Java -> stringResource(SharedRes.string.java)
        ConfigurationTab.GameOptions -> stringResource(ConfigRes.string.gameOptions)
        ConfigurationTab.NativeLibraries -> stringResource(ConfigRes.string.nativeLibraries)
        ConfigurationTab.Storage -> stringResource(ConfigRes.string.storage)
        ConfigurationTab.Advanced -> stringResource(SharedRes.string.advanced)
        ConfigurationTab.About -> stringResource(ConfigRes.string.about)
    }
