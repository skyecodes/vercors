package app.vercors.feature.theme.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class ThemeId(val value: String)

data class AppThemeDefinition(
    val id: ThemeId,
    val displayName: String,
    val colors: ThemeColors,
    val source: ThemeSource,
)

enum class ThemeSource {
    BuiltIn,
    Custom
}

data class ThemeColors(
    val background: Long,
    val backgroundAlt: Long,
    val onBackground: Long,
    val primary: Long,
    val onPrimary: Long,
    val error: Long,
    val onError: Long,
    val success: Long,
    val onSuccess: Long,
    val warn: Long,
    val surface: Long,
    val surface2: Long,
)
