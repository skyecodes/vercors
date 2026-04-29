package app.vercors.feature.theme.data.builtin

import app.vercors.feature.theme.domain.model.AppThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeColors
import app.vercors.feature.theme.domain.model.ThemeId
import app.vercors.feature.theme.domain.model.ThemeSource

object BuiltInThemes {
    val catppuccinMocha = AppThemeDefinition(
        id = ThemeId("catppuccin_mocha"),
        displayName = "Catppuccin Mocha",
        source = ThemeSource.BuiltIn,
        colors = ThemeColors(
            background = 0xff1e1e2e,
            backgroundAlt = 0xff181825,
            onBackground = 0xffcdd6f4,
            primary = 0xffcba6f7,
            onPrimary = 0xff11111b,
            error = 0xfff38ba8,
            onError = 0xff11111b,
            success = 0xffa6e3a1,
            onSuccess = 0xff11111b,
            warn = 0xfffab387,
            surface = 0xff313244,
            surface2 = 0xff45475a,
        )
    )

    val all: List<AppThemeDefinition> = listOf(catppuccinMocha)

    val byId: Map<String, AppThemeDefinition> = all.associateBy { it.id.value }
}
