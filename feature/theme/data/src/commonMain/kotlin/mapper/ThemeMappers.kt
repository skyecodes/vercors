package app.vercors.feature.theme.data.mapper

import app.vercors.feature.theme.data.dto.ThemeDefinitionData
import app.vercors.feature.theme.domain.model.*
import arrow.core.raise.context.Raise
import arrow.core.raise.context.raise

sealed interface ThemeError {
    data class InvalidId(val id: String) : ThemeError
    data class InvalidColor(val field: String, val value: String) : ThemeError
}

context(raise: Raise<ThemeError>)
fun ThemeDefinitionData.toDomain(source: ThemeSource): ThemeDefinition {
    if (id.isBlank()) raise(ThemeError.InvalidId(id))

    return ThemeDefinition(
        id = id,
        displayName = displayName.ifBlank { id },
        isDark = isDark,
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
            surface3 = parseColor("surface3", colors.surface3)
        ),
        accents = accents.map {
            ThemeNamedColor(
                id = it.id,
                name = it.name,
                color = parseColorArgb("accents.$id", it.color),
            )
        },
        defaultAccent = defaultAccent,
        source = source
    )
}

context(raise: Raise<ThemeError>)
private fun parseColor(field: String, value: String): ThemeColor = when {
    value.startsWith("#") -> parseColorArgb(field, value)
    value.startsWith("$") -> ThemeColor.Named(value.removePrefix("$"))
    else -> raise(ThemeError.InvalidColor(field, value))
}

context(raise: Raise<ThemeError>)
private fun parseColorArgb(field: String, hex: String): ThemeColor.Argb {
    val cleaned = hex.removePrefix("#")
    return when (cleaned.length) {
        6 -> ThemeColor.Argb(0xFF000000 or cleaned.toLong(16))
        8 -> ThemeColor.Argb(cleaned.toLong(16))
        else -> raise(ThemeError.InvalidColor(field, hex))
    }
}
