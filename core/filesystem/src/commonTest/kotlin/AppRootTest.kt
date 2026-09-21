package app.vercors.core.filesystem

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppRootTest {
    private fun freshDefaultRoot(name: String): Path {
        val dir = Path("build/app-root-test/$name")
        if (SystemFileSystem.exists(dir)) dir.deleteRecursivelyBlocking()
        return dir
    }

    private fun Path.deleteRecursivelyBlocking() {
        if (SystemFileSystem.metadataOrNull(this)?.isDirectory == true) {
            SystemFileSystem.list(this).forEach { it.deleteRecursivelyBlocking() }
        }
        SystemFileSystem.delete(this)
    }

    @Test
    fun `readAppRoot returns null before setup has ever written a marker`() {
        val defaultRoot = freshDefaultRoot("unconfigured")
        assertNull(readAppRoot(defaultRoot))
    }

    @Test
    fun `writeAppRoot then readAppRoot round-trips the chosen root even when it equals the default`() {
        val defaultRoot = freshDefaultRoot("round-trip")
        val chosen = Path("build/app-root-test/round-trip/elsewhere")
        writeAppRoot(defaultRoot, chosen)
        assertEquals(chosen.toString(), readAppRoot(defaultRoot).toString())

        writeAppRoot(defaultRoot, defaultRoot)
        assertEquals(defaultRoot.toString(), readAppRoot(defaultRoot).toString())
    }

    @Test
    fun `ensureRootReachable creates the directory and returns true`() {
        val root = freshDefaultRoot("reachable")
        assertTrue(!SystemFileSystem.exists(root))
        assertTrue(ensureRootReachable(root))
        assertTrue(SystemFileSystem.exists(root))
    }
}
