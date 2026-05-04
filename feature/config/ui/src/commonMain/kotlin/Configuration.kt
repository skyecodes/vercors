package app.vercors.feature.config.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppCheckbox
import app.vercors.core.ui.component.AppSelectField
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.theme.label
import app.vercors.core.ui.theme.textStyles
import app.vercors.core.ui.theme.title
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.Store
import com.composeunstyled.theme.Theme
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@CircuitInject(ConfigurationScreen::class, AppScope::class)
@Composable
fun Configuration(
    store: Store<ConfigurationState, ConfigurationEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store
    with(eventSink) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            UIConfigurationCategory(
                state = state
            )
        }
    }
}

context(eventSink: EventSink<ConfigurationEvent>)
private fun LazyListScope.UIConfigurationCategory(
    state: ConfigurationState
) {
    ConfigurationCategory(
        titleRes = ConfigRes.string.ui,
    ) {
        if (state.selectedTheme != null) {
            ConfigurationRow(
                title = ConfigRes.string.theme,
                description = ConfigRes.string.themeDesc,
            ) {
                AppSelectField(
                    value = state.selectedTheme,
                    items = state.themeList,
                    onValueChange = { eventSink(ConfigurationEvent.SelectTheme(it.id)) },
                    itemRenderer = { theme, _ ->
                        AppText(text = theme.displayName)
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
                        onValueChange = { eventSink(ConfigurationEvent.SelectAccent(it.id)) },
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
                    onCheckedChange = { eventSink(ConfigurationEvent.ToggleAnimations) }
                )
            }
        }
    }
}

private fun LazyListScope.ConfigurationCategory(
    titleRes: StringResource,
    content: LazyListScope.() -> Unit
) {
    stickyHeader(titleRes) {
        AppText(
            text = stringResource(titleRes),
            style = Theme[textStyles][title],
            modifier = Modifier.padding(start = 16.dp)
        )
    }
    content()
}

private fun LazyListScope.ConfigurationRow(
    title: StringResource,
    description: StringResource,
    content: @Composable RowScope.() -> Unit
) {
    item(title.key) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AppText(text = stringResource(title), fontWeight = FontWeight.SemiBold)
                AppText(text = stringResource(description), style = Theme[textStyles][label])
            }
            content()
        }
    }
}
