package app.vercors.feature.instance.domain.model.options

private const val MODERN_KEY_PREFIX = "key."
private const val MODERN_KEYBOARD_PREFIX = "key.keyboard."
private const val MODERN_MOUSE_PREFIX = "key.mouse."
private const val MODERN_UNBOUND = "key.keyboard.unknown"
private const val LEGACY_UNBOUND = 0

// LWJGL2 stored mouse buttons as button index - 100
private const val LEGACY_MOUSE_OFFSET = -100
private val MODERN_NAMED_MOUSE_BUTTONS = listOf("left", "right", "middle")

// LWJGL2 Keyboard.KEY_* codes to the GLFW key names Minecraft writes since 17w43a
private val LEGACY_TO_MODERN_KEYS: Map<Int, String> = buildMap {
    put(1, "escape")
    ('1'..'9').forEachIndexed { index, digit -> put(2 + index, digit.toString()) }
    put(11, "0")
    put(12, "minus")
    put(13, "equal")
    put(14, "backspace")
    put(15, "tab")
    "qwertyuiop".forEachIndexed { index, letter -> put(16 + index, letter.toString()) }
    put(26, "left.bracket")
    put(27, "right.bracket")
    put(28, "enter")
    put(29, "left.control")
    "asdfghjkl".forEachIndexed { index, letter -> put(30 + index, letter.toString()) }
    put(39, "semicolon")
    put(40, "apostrophe")
    put(41, "grave.accent")
    put(42, "left.shift")
    put(43, "backslash")
    "zxcvbnm".forEachIndexed { index, letter -> put(44 + index, letter.toString()) }
    put(51, "comma")
    put(52, "period")
    put(53, "slash")
    put(54, "right.shift")
    put(55, "keypad.multiply")
    put(56, "left.alt")
    put(57, "space")
    put(58, "caps.lock")
    (1..10).forEach { put(58 + it, "f$it") }
    put(69, "num.lock")
    put(70, "scroll.lock")
    put(71, "keypad.7")
    put(72, "keypad.8")
    put(73, "keypad.9")
    put(74, "keypad.subtract")
    put(75, "keypad.4")
    put(76, "keypad.5")
    put(77, "keypad.6")
    put(78, "keypad.add")
    put(79, "keypad.1")
    put(80, "keypad.2")
    put(81, "keypad.3")
    put(82, "keypad.0")
    put(83, "keypad.decimal")
    put(87, "f11")
    put(88, "f12")
    (13..18).forEach { put(87 + it, "f$it") }
    put(113, "f19")
    put(141, "keypad.equal")
    put(156, "keypad.enter")
    put(157, "right.control")
    put(181, "keypad.divide")
    put(183, "print.screen")
    put(184, "right.alt")
    put(197, "pause")
    put(199, "home")
    put(200, "up")
    put(201, "page.up")
    put(203, "left")
    put(205, "right")
    put(207, "end")
    put(208, "down")
    put(209, "page.down")
    put(210, "insert")
    put(211, "delete")
    put(219, "left.win")
    put(220, "right.win")
    put(221, "menu")
}

private val MODERN_TO_LEGACY_KEYS: Map<String, Int> =
    LEGACY_TO_MODERN_KEYS.entries.associate { (code, name) -> name to code }

fun GameOptions.keybindFormat(): KeybindFormat? {
    val keys = values.filterKeys { it.startsWith(KEYBIND_PREFIX) }.values.map { it.keybindKey() }
    return when {
        keys.any { it.startsWith(MODERN_KEY_PREFIX) } -> KeybindFormat.Modern
        keys.any { it.toIntOrNull() != null } -> KeybindFormat.Legacy
        else -> null
    }
}

// Keybinds without an equivalent in the target format are dropped, so a merge keeps the target's own binding
fun GameOptions.convertKeybinds(target: KeybindFormat): GameOptions = copy(
    values = values.mapNotNull { (key, value) ->
        if (!key.startsWith(KEYBIND_PREFIX)) return@mapNotNull key to value
        val converted = when (target) {
            KeybindFormat.Modern -> value.keybindKey().toIntOrNull()?.let(::legacyToModernKey)
            KeybindFormat.Legacy -> legacyKeyOrNull(value.keybindKey())?.toString()
        } ?: return@mapNotNull null
        key to converted + value.keybindModifierSuffix()
    }.toMap()
)

private fun legacyToModernKey(code: Int): String? = when {
    code == LEGACY_UNBOUND -> MODERN_UNBOUND
    code < 0 -> {
        val button = code - LEGACY_MOUSE_OFFSET
        when {
            button < 0 -> null
            button < MODERN_NAMED_MOUSE_BUTTONS.size -> MODERN_MOUSE_PREFIX + MODERN_NAMED_MOUSE_BUTTONS[button]
            else -> MODERN_MOUSE_PREFIX + (button + 1)
        }
    }

    else -> LEGACY_TO_MODERN_KEYS[code]?.let { MODERN_KEYBOARD_PREFIX + it }
}

private fun legacyKeyOrNull(key: String): Int? = when {
    key == MODERN_UNBOUND -> LEGACY_UNBOUND
    key.startsWith(MODERN_MOUSE_PREFIX) -> {
        val button = key.removePrefix(MODERN_MOUSE_PREFIX)
        val index = MODERN_NAMED_MOUSE_BUTTONS.indexOf(button).takeIf { it >= 0 }
            ?: button.toIntOrNull()?.minus(1)?.takeIf { it >= MODERN_NAMED_MOUSE_BUTTONS.size }
        index?.let { LEGACY_MOUSE_OFFSET + it }
    }

    key.startsWith(MODERN_KEYBOARD_PREFIX) -> MODERN_TO_LEGACY_KEYS[key.removePrefix(MODERN_KEYBOARD_PREFIX)]
    else -> null
}

// Forge and NeoForge append the key modifier to the binding: `key_key.jei.showRecipe:34:CONTROL`
private fun String.keybindKey(): String = substringBefore(':').trim()

private fun String.keybindModifierSuffix(): String = if (':' in this) ":" + substringAfter(':') else ""
