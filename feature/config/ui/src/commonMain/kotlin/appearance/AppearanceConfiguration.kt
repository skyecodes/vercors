package app.vercors.feature.config.ui.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppCheckbox
import app.vercors.core.ui.component.AppSelectField
import app.vercors.core.ui.component.AppSelectItem
import app.vercors.core.ui.component.AppText
import app.vercors.feature.config.ui.*
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.Store
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sun

private val UI_SCALE_OPTIONS = listOf(70, 80, 90, 100, 110, 125, 150, 175, 200)

@Composable
fun AppearanceConfiguration(
    content: Store<AppearanceConfigurationState, AppearanceConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        AppearanceConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<AppearanceConfigurationEvent>)
private fun AppearanceConfigurationContent(state: AppearanceConfigurationState, horizontalTabs: Boolean?) {
    ConfigurationTabScaffold(horizontalTabs) {
        if (state.selectedTheme != null) {
            ConfigurationRow(
                title = ConfigRes.string.theme,
                description = ConfigRes.string.themeDesc,
            ) {
                AppSelectField(
                    value = state.selectedTheme,
                    items = state.themeList,
                    onValueChange = { eventSink(AppearanceConfigurationEvent.OnThemeChange(it.id)) },
                    itemRenderer = { theme, _ ->
                        AppSelectItem(
                            text = theme.displayName,
                            icon = if (theme.isDark) Lucide.Moon else Lucide.Sun
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            if (state.selectedAccent != null) {
                ConfigurationRow(
                    title = ConfigRes.string.accent,
                    description = ConfigRes.string.accentDesc,
                ) {
                    AppSelectField(
                        value = state.selectedAccent,
                        items = state.selectedTheme.accents,
                        onValueChange = { eventSink(AppearanceConfigurationEvent.OnAccentChange(it.id)) },
                        itemRenderer = { accent, _ ->
                            Box(modifier = Modifier.size(16.dp).background(accent.color))
                            AppText(text = accent.name)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (state.animations != null) {
            ConfigurationRow(
                title = ConfigRes.string.animations,
                description = ConfigRes.string.animationsDesc,
            ) {
                AppCheckbox(
                    checked = state.animations,
                    onCheckedChange = { eventSink(AppearanceConfigurationEvent.OnAnimationsToggle) }
                )
            }
        }
        if (state.uiScalePercent != null) {
            ConfigurationRow(
                title = ConfigRes.string.uiScale,
                description = ConfigRes.string.uiScaleDesc,
            ) {
                AppSelectField(
                    value = state.uiScalePercent,
                    items = UI_SCALE_OPTIONS,
                    onValueChange = { eventSink(AppearanceConfigurationEvent.OnUiScalePercentChange(it)) },
                    itemRenderer = { percent, _ -> AppSelectItem(text = "$percent%") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.horizontalTabs != null) {
            ConfigurationRow(
                title = ConfigRes.string.horizontalTabs,
                description = ConfigRes.string.horizontalTabsDesc,
            ) {
                AppCheckbox(
                    checked = state.horizontalTabs,
                    onCheckedChange = { eventSink(AppearanceConfigurationEvent.OnHorizontalTabsToggle) }
                )
            }
        }
    }
}
