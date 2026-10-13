package app.vercors.shared.data

import app.vercors.core.filesystem.createDirectories
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.writeText
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ZipWriterTest {
    private val dir = Path("build/zip-writer-test")

    @Test
    fun `writes nested entries readable by kzip`() = runTest {
        dir.createDirectories()
        val source = Path(dir, "source.txt")
        source.writeText("file content")
        val target = Path(dir, "nested.zip")

        target.writeZipAtomically {
            addBytes("root.json", "{}".encodeToByteArray())
            addFile("overrides/config/source.txt", source)
        }

        val entries = mutableMapOf<String, String>()
        Zip.open(target).use { zip ->
            zip.forEachEntry { entry ->
                if (!entry.isDirectory) entries[entry.path.toZipEntryName()] = entry.readToBytes().decodeToString()
            }
        }
        assertEquals(mapOf("root.json" to "{}", "overrides/config/source.txt" to "file content"), entries)
        assertFalse(Path("$target.tmp").exists())
    }

    @Test
    fun `leaves no file behind when writing fails`() = runTest {
        dir.createDirectories()
        val target = Path(dir, "failed.zip")

        assertFailsWith<IllegalStateException> {
            target.writeZipAtomically {
                addBytes("a.txt", byteArrayOf(1))
                error("boom")
            }
        }

        assertFalse(target.exists())
        assertFalse(Path("$target.tmp").exists())
    }
}
