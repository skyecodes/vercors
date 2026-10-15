package app.vercors.feature.instance.ui.options.copy

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.options.OptionsCategoryCheckboxes
import app.vercors.shared.ui.*
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

private val COPY_OPTIONS_FORM_WIDTH = 400.dp

@Composable
fun CopyOptions(
    store: Store<CopyOptionsState, CopyOptionsEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    AppDialog(
        modifier = modifier,
        onClickOutside = { if (!state.isCopying) eventSink(CopyOptionsEvent.OnCancelClick) }
    ) {
        with(eventSink) {
            AppForm(modifier = Modifier.width(COPY_OPTIONS_FORM_WIDTH)) {
                AppText(
                    text = stringResource(InstanceRes.string.copyOptionsFrom),
                    style = Theme[textStyles][title]
                )

                CopyOptionsSourceSection(state)
                CopyOptionsCategoriesSection(state)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppIconTextButton(
                        onClick = { eventSink(CopyOptionsEvent.OnCancelClick) },
                        text = stringResource(SharedRes.string.cancel),
                        imageVector = Lucide.X,
                        enabled = !state.isCopying,
                    )
                    AppIconTextButton(
                        onClick = { eventSink(CopyOptionsEvent.OnCopyClick) },
                        text = stringResource(SharedRes.string.copy),
                        imageVector = Lucide.Copy,
                        backgroundColor = Theme[colors][primary],
                        contentColor = Theme[colors][onPrimary],
                        enabled = state.canCopy,
                    )
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<CopyOptionsEvent>)
private fun CopyOptionsSourceSection(state: CopyOptionsState) {
    val sources = state.sources
    when {
        sources == null -> AppLoadingPlaceholder(modifier = Modifier.fillMaxWidth().height(40.dp))
        sources.isEmpty() -> AppText(
            text = stringResource(InstanceRes.string.noOptionsSources),
            style = Theme[textStyles][label]
        )

        else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppSelectField(
                label = stringResource(InstanceRes.string.copyOptionsSource),
                value = state.selectedSource,
                items = sources,
                onValueChange = { source -> source?.let { eventSink(CopyOptionsEvent.OnSourceSelected(it)) } },
                itemRenderer = { value, _ -> AppSelectItem(text = value?.title.orEmpty()) },
                modifier = Modifier.fillMaxWidth(),
            )
            val selectedSource = state.selectedSource
            if (state.versionMismatch && selectedSource != null) {
                AppText(
                    text = stringResource(
                        InstanceRes.string.optionsVersionMismatch,
                        selectedSource.title,
                        selectedSource.gameVersion,
                        state.targetGameVersion
                    ),
                    style = Theme[textStyles][label],
                    color = Theme[colors][warn],
                )
            }
        }
    }
}

@Composable
context(eventSink: EventSink<CopyOptionsEvent>)
private fun CopyOptionsCategoriesSection(state: CopyOptionsState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppText(text = stringResource(InstanceRes.string.optionsCategories), fontWeight = FontWeight.Medium)
        OptionsCategoryCheckboxes(
            selected = state.categories,
            onToggle = { category, checked -> eventSink(CopyOptionsEvent.OnCategoryToggle(category, checked)) },
        )
        if (state.replacesFiles) {
            AppText(
                text = stringResource(InstanceRes.string.optionsReplaceFilesNotice),
                style = Theme[textStyles][label]
            )
        }
    }
}

private val CopyOptionsSourceView.title: String
    @Composable get() = name ?: stringResource(InstanceRes.string.defaultOptionsSource)
