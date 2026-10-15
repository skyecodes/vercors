package app.vercors.feature.config.ui.gameoptions

import androidx.compose.runtime.Composable
import app.vercors.core.ui.component.AppCheckbox
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.error
import app.vercors.core.ui.theme.onError
import app.vercors.feature.config.ui.*
import app.vercors.feature.instance.ui.options.title
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.overlay.ConfirmDialog
import app.vercors.shared.ui.overlay.ConfirmDialogButton
import app.vercors.shared.ui.overlay.ConfirmDialogButtonStyle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash
import com.composeunstyled.theme.Theme
import com.slack.circuit.overlay.OverlayEffect
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameOptionsConfiguration(
    content: Store<GameOptionsConfigurationState, GameOptionsConfigurationEvent>,
    horizontalTabs: Boolean?,
) {
    val (state, eventSink) = content
    with(eventSink) {
        if (state.promptClearDefaultOptions) {
            OverlayEffect {
                val result = show(
                    ConfirmDialog(
                        messageResource = ConfigRes.string.clearDefaultOptionsConfirmation,
                        actionButton = ConfirmDialogButton(
                            ConfigRes.string.clearDefaultOptions,
                            Lucide.Trash,
                            ConfirmDialogButtonStyle.Destructive
                        ),
                    )
                )
                eventSink(GameOptionsConfigurationEvent.OnConfirmClear(result))
            }
        }

        GameOptionsConfigurationContent(state, horizontalTabs)
    }
}

@Composable
context(eventSink: EventSink<GameOptionsConfigurationEvent>)
private fun GameOptionsConfigurationContent(state: GameOptionsConfigurationState, horizontalTabs: Boolean?) {
    ConfigurationTabScaffold(horizontalTabs) {
        val defaultOptions = state.defaultOptions
        if (defaultOptions == null) {
            ConfigurationRow(
                title = stringResource(ConfigRes.string.defaultOptions),
                description = stringResource(ConfigRes.string.defaultOptionsNotSetDesc),
            ) {}
            return@ConfigurationTabScaffold
        }
        ConfigurationRow(
            title = stringResource(ConfigRes.string.defaultOptions),
            description = stringResource(
                ConfigRes.string.defaultOptionsSetDesc,
                defaultOptions.sourceInstanceName,
                defaultOptions.gameVersion,
                HumanReadable.timeAgo(defaultOptions.capturedAt),
                defaultOptions.categories.map { it.title }.joinToString(", "),
            ),
        ) {
            AppIconTextButton(
                onClick = { eventSink(GameOptionsConfigurationEvent.OnClearClick) },
                text = stringResource(ConfigRes.string.clearDefaultOptions),
                imageVector = Lucide.Trash,
                backgroundColor = Theme[colors][error],
                contentColor = Theme[colors][onError]
            )
        }
        ConfigurationRow(
            title = stringResource(ConfigRes.string.applyDefaultOptions),
            description = stringResource(ConfigRes.string.applyDefaultOptionsDesc),
        ) {
            AppCheckbox(
                checked = defaultOptions.autoApply,
                onCheckedChange = { eventSink(GameOptionsConfigurationEvent.OnAutoApplyToggle) }
            )
        }
    }
}
