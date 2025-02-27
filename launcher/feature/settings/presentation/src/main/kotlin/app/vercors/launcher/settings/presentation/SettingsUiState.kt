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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import app.vercors.launcher.core.config.model.*
import app.vercors.launcher.core.presentation.theme.CatppuccinColors

@Immutable
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Loaded(
        val general: GeneralConfigUi,
        val home: HomeConfigUi
    ) : SettingsUiState
}

data class GeneralConfigUi(
    val theme: String,
    val accent: String,
    val gradient: Boolean,
    val decorated: Boolean,
    val animations: Boolean,
    val defaultTab: TabConfig,
    val themes: List<ThemeUi> = ThemeUi.DEFAULT_LIST,
    val accentColors: List<AccentColorUi> = AccentColorUi.DEFAULT_LIST
) {
    constructor(config: GeneralConfig) : this(
        config.theme,
        config.accent,
        config.gradient,
        config.decorated,
        config.animations,
        config.defaultTab
    )

    val currentTheme: ThemeUi = themes.first { it.id == theme }
    val currentAccentColor: AccentColorUi = accentColors.first { it.id == accent }
}

data class HomeConfigUi(
    val sections: List<HomeSectionConfig>,
    val provider: HomeProviderConfig
) {
    constructor(config: HomeConfig) : this(config.sections, config.provider)
}

data class AccentColorUi(
    val id: String,
    val name: String,
    val color: (CatppuccinColors) -> Color
) {
    companion object {
        val DEFAULT_LIST = listOf(
            AccentColorUi("rosewater", "Rosewater", CatppuccinColors::rosewater),
            AccentColorUi("flamingo", "Flamingo", CatppuccinColors::flamingo),
            AccentColorUi("pink", "Pink", CatppuccinColors::pink),
            AccentColorUi("mauve", "Mauve", CatppuccinColors::mauve),
            AccentColorUi("red", "Red", CatppuccinColors::red),
            AccentColorUi("maroon", "Maroon", CatppuccinColors::maroon),
            AccentColorUi("peach", "Peach", CatppuccinColors::peach),
            AccentColorUi("yellow", "Yellow", CatppuccinColors::yellow),
            AccentColorUi("green", "Green", CatppuccinColors::green),
            AccentColorUi("teal", "Teal", CatppuccinColors::teal),
            AccentColorUi("sky", "Sky", CatppuccinColors::sky),
            AccentColorUi("sapphire", "Sapphire", CatppuccinColors::sapphire),
            AccentColorUi("blue", "Blue", CatppuccinColors::blue),
            AccentColorUi("lavender", "Lavender", CatppuccinColors::lavender),
        )
    }
}

data class ThemeUi(
    val id: String,
    val name: String,
) {
    companion object {
        val DEFAULT_LIST = listOf(
            ThemeUi("catppuccin-latte", "Catppuccin Latte"),
            ThemeUi("catppuccin-frappe", "Catppuccin Frappe"),
            ThemeUi("catppuccin-macchiato", "Catppuccin Macchiato"),
            ThemeUi("catppuccin-mocha", "Catppuccin Mocha"),
        )
    }
}
