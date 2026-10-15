package app.vercors.feature.instance.domain.model.options

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KeybindUtilsTest {
    private fun options(vararg values: Pair<String, String>) = GameOptions(version = null, values = mapOf(*values))

    @Test
    fun `detects the keybind format from the values`() {
        assertEquals(KeybindFormat.Modern, options("key_key.jump" to "key.keyboard.space").keybindFormat())
        assertEquals(KeybindFormat.Legacy, options("key_key.jump" to "57").keybindFormat())
        assertEquals(KeybindFormat.Legacy, options("key_key.jei.showRecipe" to "19:CONTROL").keybindFormat())
        assertNull(options("fov" to "70").keybindFormat())
    }

    @Test
    fun `converts legacy keyboard codes to named keys`() {
        val converted = options(
            "key_key.jump" to "57",
            "key_key.forward" to "17",
            "key_key.inventory" to "18",
            "key_key.hotbar.1" to "2",
            "key_key.screenshot" to "60",
            "key_key.sprint" to "29",
            "key_key.chat" to "20",
            "fov" to "70",
        ).convertKeybinds(KeybindFormat.Modern)
        assertEquals(
            mapOf(
                "key_key.jump" to "key.keyboard.space",
                "key_key.forward" to "key.keyboard.w",
                "key_key.inventory" to "key.keyboard.e",
                "key_key.hotbar.1" to "key.keyboard.1",
                "key_key.screenshot" to "key.keyboard.f2",
                "key_key.sprint" to "key.keyboard.left.control",
                "key_key.chat" to "key.keyboard.t",
                "fov" to "70",
            ),
            converted.values
        )
    }

    @Test
    fun `converts mouse buttons and unbound keys both ways`() {
        val legacy = options(
            "key_key.attack" to "-100",
            "key_key.use" to "-99",
            "key_key.pickItem" to "-98",
            "key_key.mod.dash" to "-97",
            "key_key.mod.unused" to "0",
        )
        val modern = options(
            "key_key.attack" to "key.mouse.left",
            "key_key.use" to "key.mouse.right",
            "key_key.pickItem" to "key.mouse.middle",
            "key_key.mod.dash" to "key.mouse.4",
            "key_key.mod.unused" to "key.keyboard.unknown",
        )
        assertEquals(modern, legacy.convertKeybinds(KeybindFormat.Modern))
        assertEquals(legacy, modern.convertKeybinds(KeybindFormat.Legacy))
    }

    @Test
    fun `keeps Forge key modifiers when converting`() {
        assertEquals(
            options("key_key.jei.showRecipe" to "key.keyboard.r:CONTROL"),
            options("key_key.jei.showRecipe" to "19:CONTROL").convertKeybinds(KeybindFormat.Modern)
        )
        assertEquals(
            options("key_key.jei.showRecipe" to "19:CONTROL"),
            options("key_key.jei.showRecipe" to "key.keyboard.r:CONTROL").convertKeybinds(KeybindFormat.Legacy)
        )
    }

    @Test
    fun `drops keybinds that have no equivalent in the target format`() {
        val converted = options(
            "key_key.jump" to "key.keyboard.world.1",
            "key_key.sneak" to "key.keyboard.f25",
            "key_key.drop" to "key.keyboard.q",
        ).convertKeybinds(KeybindFormat.Legacy)
        assertEquals(mapOf("key_key.drop" to "16"), converted.values)
        assertEquals(emptyMap(), options("key_key.jump" to "112").convertKeybinds(KeybindFormat.Modern).values)
    }

    @Test
    fun `every mapped legacy code survives a round trip`() {
        val all = GameOptions(null, (1..255).associate { "key_$it" to "$it" })
        val legacy = all.copy(values = all.values.filterKeys { it in all.convertKeybinds(KeybindFormat.Modern).values })
        assertEquals(112, legacy.values.size)
        assertEquals(legacy, legacy.convertKeybinds(KeybindFormat.Modern).convertKeybinds(KeybindFormat.Legacy))
    }

    @Test
    fun `a merge keeps the target binding for keys dropped by the conversion`() {
        val target = GameOptions(3953, mapOf("key_key.jump" to "key.keyboard.space"))
        val source = options("key_key.jump" to "112", "key_key.drop" to "16").convertKeybinds(KeybindFormat.Modern)
        assertEquals(
            mapOf("key_key.jump" to "key.keyboard.space", "key_key.drop" to "key.keyboard.q"),
            target.mergeFrom(source, setOf(OptionsCategory.Keybinds)).values
        )
    }
}
