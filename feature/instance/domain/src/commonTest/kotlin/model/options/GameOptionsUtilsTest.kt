package app.vercors.feature.instance.domain.model.options

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GameOptionsUtilsTest {
    @Test
    fun `parses the version line separately and splits on the first colon only`() {
        val options = parseGameOptions("version:3953\nlastServer:play.example.com:25565\nfov:0.5\n\nbroken line\n")
        assertEquals(3953, options.version)
        assertEquals(mapOf("lastServer" to "play.example.com:25565", "fov" to "0.5"), options.values)
    }

    @Test
    fun `formats back to the same text it parsed`() {
        val text = "version:3953\nkey_key.jump:key.keyboard.space\nfov:0.5\n"
        assertEquals(text, parseGameOptions(text).format())
    }

    @Test
    fun `maps keys to their category`() {
        assertEquals(OptionsCategory.Keybinds, optionsCategoryOf("key_key.jei.showRecipe"))
        assertEquals(OptionsCategory.Audio, optionsCategoryOf("soundCategory_music"))
        assertEquals(OptionsCategory.Video, optionsCategoryOf("renderDistance"))
        assertEquals(OptionsCategory.Skin, optionsCategoryOf("modelPart_cape"))
        assertEquals(OptionsCategory.ResourcePacks, optionsCategoryOf("resourcePacks"))
        assertEquals(OptionsCategory.Other, optionsCategoryOf("tutorialStep"))
        assertNull(optionsCategoryOf("version"))
        assertNull(optionsCategoryOf("lastServer"))
    }

    @Test
    fun `merging overwrites only the selected categories and keeps the target version`() {
        val target = GameOptions(
            version = 3465,
            values = mapOf(
                "key_key.jump" to "key.keyboard.space",
                "renderDistance" to "12",
                "key_key.mod" to "key.keyboard.g"
            )
        )
        val source = GameOptions(
            version = 3953,
            values = mapOf("key_key.jump" to "key.keyboard.j", "renderDistance" to "32", "fov" to "0.8")
        )
        val merged = target.mergeFrom(source, setOf(OptionsCategory.Keybinds))
        assertEquals(3465, merged.version)
        assertEquals(
            mapOf("key_key.jump" to "key.keyboard.j", "renderDistance" to "12", "key_key.mod" to "key.keyboard.g"),
            merged.values
        )
    }

    @Test
    fun `merging into a missing file takes the source version`() {
        val source = GameOptions(version = 3953, values = mapOf("fov" to "0.8", "lang" to "fr_fr"))
        val merged = null.mergeFrom(source, setOf(OptionsCategory.Video))
        assertEquals(GameOptions(3953, mapOf("fov" to "0.8")), merged)
    }
}
