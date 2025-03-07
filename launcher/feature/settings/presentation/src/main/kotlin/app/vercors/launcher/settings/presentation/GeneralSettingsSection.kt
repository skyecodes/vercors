/*
 * Copyright (c) 2024 skyecodes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package app.vercors.launcher.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.vercors.launcher.core.config.model.TabConfig
import app.vercors.launcher.core.presentation.theme.LocalCatppuccinColors
import app.vercors.launcher.core.resources.*
import app.vercors.launcher.settings.presentation.entry.ComboboxSettingsEntry
import app.vercors.launcher.settings.presentation.entry.SwitchSettingsEntry

@Composable
fun GeneralSettingsSection(
    config: GeneralConfigUi,
    onIntent: (SettingsUiIntent) -> Unit
) {
    SettingsSection(title = appStringResource { general }) {
        ComboboxSettingsEntry(
            title = appStringResource { theme },
            description = appStringResource { theme_description },
            options = config.themes,
            value = config.currentTheme,
            textConverter = { it.name },
            onValueChange = { onIntent(SettingsUiIntent.SelectTheme(it.id)) }
        ) {
            Text(text = it.name)
        }
        ComboboxSettingsEntry(
            title = appStringResource { accent },
            description = appStringResource { accent_description },
            options = config.accentColors,
            value = config.currentAccentColor,
            textConverter = { it.name },
            leadingIcon = { ColorSquare(MaterialTheme.colorScheme.primary) },
            onValueChange = { onIntent(SettingsUiIntent.SelectAccent(it.id)) }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ColorSquare(LocalCatppuccinColors.current.run(it.color))
                Text(text = it.name)
            }
        }
        SwitchSettingsEntry(
            title = appStringResource { gradient },
            description = appStringResource { gradient_description },
            checked = config.gradient,
            onCheckedChange = { onIntent(SettingsUiIntent.ToggleGradient(it)) }
        )
        SwitchSettingsEntry(
            title = appStringResource { decorated },
            description = appStringResource { decorated_description },
            checked = config.decorated,
            onCheckedChange = { onIntent(SettingsUiIntent.ToggleDecorated(it)) }
        )
        SwitchSettingsEntry(
            title = appStringResource { animations },
            description = appStringResource { animations_description },
            checked = config.animations,
            onCheckedChange = { onIntent(SettingsUiIntent.ToggleAnimations(it)) }
        )
        ComboboxSettingsEntry(
            title = appStringResource { default_tab },
            description = appStringResource { default_tab_description },
            options = TabConfig.entries,
            value = config.defaultTab,
            textConverter = { it.displayName },
            leadingIcon = {
                Icon(
                    imageVector = config.defaultTab.icon,
                    contentDescription = config.defaultTab.displayName
                )
            },
            onValueChange = { onIntent(SettingsUiIntent.SelectDefaultTab(it)) }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = it.icon,
                    contentDescription = it.displayName
                )
                Text(text = it.displayName)
            }
        }
    }
}

@Composable
private fun ColorSquare(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(16.dp).background(color)
    )
}