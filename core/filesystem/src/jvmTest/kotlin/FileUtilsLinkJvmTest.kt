package app.vercors.core.filesystem

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.io.path.Path as JvmPath

class FileUtilsLinkJvmTest {
    @Test
    fun `linkOrCopyTo creates a real hardlink, not a copy`() = runTest {
        val dir = Path("build/link-test-hardlink")
        if (dir.exists()) dir.deleteRecursively()
        dir.createDirectories()
        val source = Path(dir, "source.txt")
        val target = Path(dir, "target.txt")
        source.writeText("hello world")

        source.linkOrCopyTo(target)

        val sourceAttrs = Files.readAttributes(JvmPath(source.toString()), "unix:ino,nlink")
        val targetAttrs = Files.readAttributes(JvmPath(target.toString()), "unix:ino,nlink")
        assertEquals(
            sourceAttrs["ino"],
            targetAttrs["ino"],
            "linked file should share the same inode as the original"
        )
        assertEquals(2, targetAttrs["nlink"], "inode's link count should reflect both directory entries")
    }
}
