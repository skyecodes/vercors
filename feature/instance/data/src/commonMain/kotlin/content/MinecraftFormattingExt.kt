package app.vercors.feature.instance.data.content

private val MINECRAFT_FORMATTING_CODE = Regex("§.")

internal fun String.stripMinecraftFormatting(): String =
    replace(MINECRAFT_FORMATTING_CODE, "").replace("§", "").ifBlank { this }
