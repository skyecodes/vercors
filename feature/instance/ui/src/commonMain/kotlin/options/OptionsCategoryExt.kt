package app.vercors.feature.instance.ui.options

import androidx.compose.runtime.Composable
import app.vercors.core.ui.component.AppCheckbox
import app.vercors.feature.instance.domain.model.options.OptionsCategory
import app.vercors.feature.instance.ui.*
import org.jetbrains.compose.resources.stringResource

val DEFAULT_SELECTED_OPTIONS_CATEGORIES: Set<OptionsCategory> = setOf(
    OptionsCategory.Keybinds,
    OptionsCategory.Video,
    OptionsCategory.Audio,
    OptionsCategory.Controls,
    OptionsCategory.Chat,
    OptionsCategory.Accessibility,
    OptionsCategory.Language,
    OptionsCategory.Skin,
)

val OptionsCategory.title: String
    @Composable get() = when (this) {
        OptionsCategory.Keybinds -> stringResource(InstanceRes.string.optionsCategoryKeybinds)
        OptionsCategory.Video -> stringResource(InstanceRes.string.optionsCategoryVideo)
        OptionsCategory.Audio -> stringResource(InstanceRes.string.optionsCategoryAudio)
        OptionsCategory.Controls -> stringResource(InstanceRes.string.optionsCategoryControls)
        OptionsCategory.Chat -> stringResource(InstanceRes.string.optionsCategoryChat)
        OptionsCategory.Accessibility -> stringResource(InstanceRes.string.optionsCategoryAccessibility)
        OptionsCategory.Language -> stringResource(InstanceRes.string.optionsCategoryLanguage)
        OptionsCategory.Skin -> stringResource(InstanceRes.string.optionsCategorySkin)
        OptionsCategory.ResourcePacks -> stringResource(InstanceRes.string.optionsCategoryResourcePacks)
        OptionsCategory.Other -> stringResource(InstanceRes.string.optionsCategoryOther)
        OptionsCategory.ServerList -> stringResource(InstanceRes.string.optionsCategoryServerList)
        OptionsCategory.Hotbars -> stringResource(InstanceRes.string.optionsCategoryHotbars)
    }

@Composable
fun OptionsCategoryCheckboxes(
    selected: Set<OptionsCategory>,
    onToggle: (OptionsCategory, Boolean) -> Unit,
) {
    OptionsCategory.entries.forEach { category ->
        AppCheckbox(
            label = category.title,
            checked = category in selected,
            onCheckedChange = { onToggle(category, it) },
            invert = true,
        )
    }
}
