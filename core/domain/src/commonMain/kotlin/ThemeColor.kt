package app.vercors.core.domain

import kotlin.jvm.JvmInline

data class ThemeColors(
    val background: ThemeColor,
    val backgroundAlt: ThemeColor,
    val onBackground: ThemeColor,
    val primary: ThemeColor,
    val onPrimary: ThemeColor,
    val error: ThemeColor,
    val onError: ThemeColor,
    val success: ThemeColor,
    val onSuccess: ThemeColor,
    val warn: ThemeColor,
    val surface: ThemeColor,
    val surface2: ThemeColor,
    val surface3: ThemeColor,
)

sealed interface ThemeColor {
    @JvmInline
    value class Argb(val value: Long) : ThemeColor

    @JvmInline
    value class Named(val name: String) : ThemeColor
}
