package app.vercors.feature.theme.data.mapper

import app.vercors.feature.theme.data.dto.ThemeJsonData
import app.vercors.feature.theme.domain.model.AppThemeDefinition
import app.vercors.feature.theme.domain.model.ThemeColors
import app.vercors.feature.theme.domain.model.ThemeId
import app.vercors.feature.theme.domain.model.ThemeSource
import arrow.core.raise.context.Raise
import arrow.core.raise.context.raise

sealed interface ThemeParseError {
    data class InvalidId(val id: String) : ThemeParseError
    data class InvalidColor(val field: String, val value: String) : ThemeParseError
}

context(raise: Raise<ThemeParseError>)
fun ThemeJsonData.toDomain(source: ThemeSource): AppThemeDefinition {
    if (id.isBlank()) raise(ThemeParseError.InvalidId(id))

    return AppThemeDefinition(
        id = ThemeId(id),
        displayName = displayName.ifBlank { id },
        source = source,
        colors = ThemeColors(
            background = parseColor("background", colors.background),
            backgroundAlt = parseColor("backgroundAlt", colors.backgroundAlt),
            onBackground = parseColor("onBackground", colors.onBackground),
            primary = parseColor("primary", colors.primary),
            onPrimary = parseColor("onPrimary", colors.onPrimary),
            error = parseColor("error", colors.error),
            onError = parseColor("onError", colors.onError),
            success = parseColor("success", colors.success),
            onSuccess = parseColor("onSuccess", colors.onSuccess),
            warn = parseColor("warn", colors.warn),
            surface = parseColor("surface", colors.surface),
            surface2 = parseColor("surface2", colors.surface2),
        )
    )
}

context(raise: Raise<ThemeParseError>)
private fun parseColor(field: String, hex: String): Long {
    val cleaned = hex.removePrefix("#")
    val argb = when (cleaned.length) {
        6 -> (0xFF000000 or cleaned.toLong(16))
        8 -> cleaned.toLong(16)
        else -> raise(ThemeParseError.InvalidColor(field, hex))
    }
    return argb
}
