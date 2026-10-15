package app.vercors.feature.instance.ui.options.savedefault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.layout.AppDialog
import app.vercors.core.ui.layout.AppForm
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.ui.*
import app.vercors.feature.instance.ui.options.OptionsCategoryCheckboxes
import app.vercors.shared.ui.SharedRes
import app.vercors.shared.ui.Store
import app.vercors.shared.ui.cancel
import app.vercors.shared.ui.save
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Save
import com.composables.icons.lucide.X
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

private val SAVE_DEFAULT_OPTIONS_FORM_WIDTH = 400.dp

@Composable
fun SaveDefaultOptions(
    store: Store<SaveDefaultOptionsState, SaveDefaultOptionsEvent>,
    modifier: Modifier = Modifier,
) {
    val (state, eventSink) = store

    AppDialog(
        modifier = modifier,
        onClickOutside = { if (!state.isSaving) eventSink(SaveDefaultOptionsEvent.OnCancelClick) }
    ) {
        AppForm(modifier = Modifier.width(SAVE_DEFAULT_OPTIONS_FORM_WIDTH)) {
            AppText(
                text = stringResource(InstanceRes.string.saveOptionsAsDefault),
                style = Theme[textStyles][title]
            )
            AppText(
                text = stringResource(InstanceRes.string.defaultOptionsDescription),
                style = Theme[textStyles][label]
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppText(text = stringResource(InstanceRes.string.optionsCategories), fontWeight = FontWeight.Medium)
                OptionsCategoryCheckboxes(
                    selected = state.categories,
                    onToggle = { category, checked ->
                        eventSink(SaveDefaultOptionsEvent.OnCategoryToggle(category, checked))
                    },
                )
            }

            state.replacedSourceName?.let { name ->
                AppText(
                    text = stringResource(InstanceRes.string.defaultOptionsReplaceNotice, name),
                    style = Theme[textStyles][label],
                    color = Theme[colors][warn],
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIconTextButton(
                    onClick = { eventSink(SaveDefaultOptionsEvent.OnCancelClick) },
                    text = stringResource(SharedRes.string.cancel),
                    imageVector = Lucide.X,
                    enabled = !state.isSaving,
                )
                AppIconTextButton(
                    onClick = { eventSink(SaveDefaultOptionsEvent.OnSaveClick) },
                    text = stringResource(SharedRes.string.save),
                    imageVector = Lucide.Save,
                    backgroundColor = Theme[colors][primary],
                    contentColor = Theme[colors][onPrimary],
                    enabled = state.canSave,
                )
            }
        }
    }
}
