package app.vercors.feature.instance.ui.export

import kotlin.test.Test
import kotlin.test.assertEquals

class ExportInstanceStateTest {
    @Test
    fun `keeps the display name with its case`() {
        assertEquals("GameShow", ExportInstanceTarget("gameshow", "GameShow").toSuggestedFileName())
    }

    @Test
    fun `replaces characters file systems reject`() {
        assertEquals("A_B_ C_", ExportInstanceTarget("a-b-c", "A/B: C?").toSuggestedFileName())
    }

    @Test
    fun `falls back to the id when nothing usable is left`() {
        assertEquals("dots", ExportInstanceTarget("dots", " ... ").toSuggestedFileName())
    }
}
