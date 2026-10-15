package app.vercors.feature.instance.domain.model.options

import kotlin.time.Instant

private const val VERSION_KEY = "version"
private const val KEYBIND_PREFIX = "key_"
private const val MODERN_KEY_PREFIX = "key."

// 17w43a moved to LWJGL3: keybinds became named keys instead of LWJGL2 integer codes
val MODERN_KEYBINDS_SINCE: Instant = Instant.parse("2017-10-25T00:00:00Z")

val FILE_OPTIONS_CATEGORIES: Set<OptionsCategory> = setOf(OptionsCategory.ServerList, OptionsCategory.Hotbars)

private val EXCLUDED_KEYS = setOf("lastServer")

private val VIDEO_KEYS = setOf(
    "graphicsMode", "fancyGraphics", "renderDistance", "simulationDistance", "entityDistanceScaling", "guiScale",
    "particles", "maxFps", "enableVsync", "fullscreen", "fullscreenResolution", "fov", "gamma", "ao", "renderClouds",
    "biomeBlendRadius", "mipmapLevels", "entityShadows", "bobView", "attackIndicator", "prioritizeChunkUpdates",
    "useVbo", "fboEnable", "anaglyph3d", "showAutosaveIndicator", "inactivityFpsLimit", "menuBackgroundBlurriness",
    "cloudRange", "textureFiltering", "maxAnisotropyBit", "improvedTransparency", "chunkSectionFadeInTime",
    "vignette", "weatherRadius", "cutoutLeaves",
)

private val AUDIO_KEYS = setOf("soundDevice", "directionalAudio", "musicFrequency", "showNowPlayingToast")

private val CONTROLS_KEYS = setOf(
    "mouseSensitivity", "invertYMouse", "invertXMouse", "autoJump", "toggleCrouch", "toggleSprint", "rawMouseInput",
    "mouseWheelSensitivity", "discrete_mouse_scroll", "touchscreen", "operatorItemsTab", "allowCursorChanges",
    "toggleAttack", "toggleUse", "sprintWindow",
)

private val CHAT_KEYS = setOf(
    "chatVisibility", "chatColors", "chatLinks", "chatLinksPrompt", "chatOpacity", "chatLineSpacing",
    "textBackgroundOpacity", "backgroundForChatOnly", "chatHeightFocused", "chatHeightUnfocused", "chatScale",
    "chatWidth", "chatDelay", "onlyShowSecureChat", "hideMatchedNames", "saveChatDrafts",
)

private val ACCESSIBILITY_KEYS = setOf(
    "narrator", "narratorHotkey", "showSubtitles", "highContrast", "highContrastBlockOutline",
    "notificationDisplayTime", "panoramaScrollSpeed", "hideLightningFlashes", "darkMojangStudiosBackground",
    "darknessEffectScale", "damageTiltStrength", "glintSpeed", "glintStrength", "screenEffectScale",
    "fovEffectScale", "hideSplashTexts", "rotateWithMinecart",
)

private val LANGUAGE_KEYS = setOf("lang", "forceUnicodeFont", "japaneseGlyphVariants")

private val SKIN_KEYS = setOf("mainHand")
private const val SKIN_PREFIX = "modelPart_"

private val RESOURCE_PACKS_KEYS = setOf("resourcePacks", "incompatibleResourcePacks")

private const val SOUND_PREFIX = "soundCategory_"

fun parseGameOptions(text: String): GameOptions {
    var version: Int? = null
    val values = LinkedHashMap<String, String>()
    for (line in text.lineSequence()) {
        val separator = line.indexOf(':')
        if (separator <= 0) continue
        val key = line.take(separator)
        val value = line.substring(separator + 1)
        if (key == VERSION_KEY) version = value.trim().toIntOrNull()
        else values[key] = value
    }
    return GameOptions(version, values)
}

fun GameOptions.format(): String = buildString {
    version?.let { appendLine("$VERSION_KEY:$it") }
    values.forEach { (key, value) -> appendLine("$key:$value") }
}

fun optionsCategoryOf(key: String): OptionsCategory? = when {
    key == VERSION_KEY || key in EXCLUDED_KEYS -> null
    key.startsWith(KEYBIND_PREFIX) -> OptionsCategory.Keybinds
    key.startsWith(SOUND_PREFIX) || key in AUDIO_KEYS -> OptionsCategory.Audio
    key.startsWith(SKIN_PREFIX) || key in SKIN_KEYS -> OptionsCategory.Skin
    key in VIDEO_KEYS -> OptionsCategory.Video
    key in CONTROLS_KEYS -> OptionsCategory.Controls
    key in CHAT_KEYS -> OptionsCategory.Chat
    key in ACCESSIBILITY_KEYS -> OptionsCategory.Accessibility
    key in LANGUAGE_KEYS -> OptionsCategory.Language
    key in RESOURCE_PACKS_KEYS -> OptionsCategory.ResourcePacks
    else -> OptionsCategory.Other
}

fun GameOptions.keybindFormat(): KeybindFormat? {
    val keybinds = values.filterKeys { it.startsWith(KEYBIND_PREFIX) }.values
    return when {
        keybinds.any { it.startsWith(MODERN_KEY_PREFIX) } -> KeybindFormat.Modern
        keybinds.any { it.trim().toIntOrNull() != null } -> KeybindFormat.Legacy
        else -> null
    }
}

fun GameOptions.filterCategories(categories: Set<OptionsCategory>): GameOptions =
    copy(values = values.filterKeys { key -> optionsCategoryOf(key)?.let { it in categories } == true })

// Keeps the target's version: Minecraft upgrades options.txt from that number, so it must match the target's keys
fun GameOptions?.mergeFrom(source: GameOptions, categories: Set<OptionsCategory>): GameOptions {
    val target = this ?: GameOptions(version = source.version, values = emptyMap())
    return target.copy(values = target.values + source.filterCategories(categories).values)
}
