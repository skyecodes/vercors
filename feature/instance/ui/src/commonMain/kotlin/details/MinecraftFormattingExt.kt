package app.vercors.feature.instance.ui.details

private val MINECRAFT_FORMATTING_CODE = Regex("§.")

internal fun String.stripMinecraftFormattingForDisplay(): String =
    replace(MINECRAFT_FORMATTING_CODE, "").replace("§", "").ifBlank { this }
