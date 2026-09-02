package app.vercors.core.filesystem

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileUtilsLinkTest {
    @Test
    fun `linkOrCopyTo produces a file with the same content`() = runTest {
        val dir = Path("build/link-test-copy")
        dir.deleteRecursivelyIfExists()
        dir.createDirectories()
        val source = Path(dir, "source.txt")
        val target = Path(dir, "target.txt")
        source.writeText("hello world")

        assertTrue(source.linkOrCopyTo(target))

        assertTrue(target.exists())
        assertEquals("hello world", target.source { it.readString() })
    }

    @Test
    fun `linkRecursively mirrors a directory tree`() = runTest {
        val dir = Path("build/link-test-recursive")
        dir.deleteRecursivelyIfExists()
        val source = Path(dir, "source")
        val target = Path(dir, "target")
        Path(source, "nested").createDirectories()
        Path(source, "root.txt").writeText("root")
        Path(source, "nested", "child.txt").writeText("child")

        source.linkRecursively(target)

        assertEquals("root", Path(target, "root.txt").source { it.readString() })
        assertEquals("child", Path(target, "nested", "child.txt").source { it.readString() })
    }

    @Test
    fun `sizeRecursively sums every file in a directory tree`() = runTest {
        val dir = Path("build/link-test-size")
        dir.deleteRecursivelyIfExists()
        Path(dir, "nested").createDirectories()
        Path(dir, "a.txt").writeText("12345")
        Path(dir, "nested", "b.txt").writeText("1234567890")

        assertEquals(15L, dir.sizeRecursively())
    }

    private suspend fun Path.deleteRecursivelyIfExists() {
        if (exists()) deleteRecursively()
    }
}
