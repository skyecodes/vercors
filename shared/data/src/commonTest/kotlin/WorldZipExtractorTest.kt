package app.vercors.shared.data

import kotlin.test.Test
import kotlin.test.assertEquals

class WorldZipExtractorTest {
    @Test
    fun `returns the base name when there is no collision`() {
        assertEquals("My World", uniqueWorldFolderName(emptySet(), "My World"))
    }

    @Test
    fun `appends a suffix on the first collision`() {
        assertEquals("My World (2)", uniqueWorldFolderName(setOf("My World"), "My World"))
    }

    @Test
    fun `increments the suffix until free`() {
        val existing = setOf("My World", "My World (2)", "My World (3)")
        assertEquals("My World (4)", uniqueWorldFolderName(existing, "My World"))
    }

    @Test
    fun `ignores unrelated names`() {
        assertEquals("My World", uniqueWorldFolderName(setOf("Someone Else's World"), "My World"))
    }
}
