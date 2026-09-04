package app.vercors.feature.project.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class UniqueWorldFolderNameTest {
    @Test
    fun returnsBaseNameWhenNoCollision() {
        assertEquals("My World", uniqueWorldFolderName(emptySet(), "My World"))
    }

    @Test
    fun appendsSuffixOnFirstCollision() {
        assertEquals("My World (2)", uniqueWorldFolderName(setOf("My World"), "My World"))
    }

    @Test
    fun incrementsSuffixUntilFree() {
        val existing = setOf("My World", "My World (2)", "My World (3)")
        assertEquals("My World (4)", uniqueWorldFolderName(existing, "My World"))
    }

    @Test
    fun ignoresUnrelatedNames() {
        assertEquals("My World", uniqueWorldFolderName(setOf("Someone Else's World"), "My World"))
    }
}
