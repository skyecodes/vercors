package app.vercors.core.filesystem

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue
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

        assertTrue(
            Files.isSameFile(JvmPath(source.toString()), JvmPath(target.toString())),
            "linked file should be the same file as the original"
        )
    }
}
