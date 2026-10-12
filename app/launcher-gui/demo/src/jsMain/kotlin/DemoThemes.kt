package app.vercors.launcher.demo

import app.vercors.core.domain.ThemeColor
import app.vercors.core.domain.ThemeColors

val MOCHA_COLORS: ThemeColors = ThemeColors(
    background = ThemeColor.Argb(0xFF1E1E2E),
    backgroundAlt = ThemeColor.Argb(0xFF181825),
    onBackground = ThemeColor.Argb(0xFFCDD6F4),
    primary = ThemeColor.Named("accent"),
    onPrimary = ThemeColor.Argb(0xFF11111B),
    error = ThemeColor.Argb(0xFFF38BA8),
    onError = ThemeColor.Argb(0xFF11111B),
    success = ThemeColor.Argb(0xFFA6E3A1),
    onSuccess = ThemeColor.Argb(0xFF11111B),
    warn = ThemeColor.Argb(0xFFFAB387),
    surface = ThemeColor.Argb(0xFF313244),
    surface2 = ThemeColor.Argb(0xFF45475A),
    surface3 = ThemeColor.Argb(0xFF585B70),
)

val LATTE_COLORS: ThemeColors = ThemeColors(
    background = ThemeColor.Argb(0xFFEFF1F5),
    backgroundAlt = ThemeColor.Argb(0xFFE6E9EF),
    onBackground = ThemeColor.Argb(0xFF4C4F69),
    primary = ThemeColor.Named("accent"),
    onPrimary = ThemeColor.Argb(0xFFDCE0E8),
    error = ThemeColor.Argb(0xFFD20F39),
    onError = ThemeColor.Argb(0xFFDCE0E8),
    success = ThemeColor.Argb(0xFF40A02B),
    onSuccess = ThemeColor.Argb(0xFFDCE0E8),
    warn = ThemeColor.Argb(0xFFFE640B),
    surface = ThemeColor.Argb(0xFFCCD0DA),
    surface2 = ThemeColor.Argb(0xFFBCC0CC),
    surface3 = ThemeColor.Argb(0xFFACB0BE),
)

const val MOCHA_MAUVE: Long = 0xFFCBA6F7
const val LATTE_MAUVE: Long = 0xFF8839EF
