package app.vercors.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtTest {
    @Test
    fun `given a string, when toSlug is called, then it returns a slug`() {
        assertEquals("hello-world", "Hello World!".toSlug())
    }
}