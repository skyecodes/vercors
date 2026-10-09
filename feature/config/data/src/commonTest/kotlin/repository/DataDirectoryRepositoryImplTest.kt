package app.vercors.feature.config.data.repository

import app.vercors.core.cryptography.CryptographyServiceImpl
import app.vercors.core.filesystem.*
import app.vercors.shared.data.AppPaths
import arrow.core.raise.context.either
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataDirectoryRepositoryImplTest {
    private fun freshDir(name: String): Path {
        val dir = Path("build/data-directory-repository-test/$name")
        if (SystemFileSystem.exists(dir)) dir.deleteRecursivelyForTest()
        return dir
    }

    private fun Path.deleteRecursivelyForTest() {
        if (SystemFileSystem.metadataOrNull(this)?.isDirectory == true) {
            SystemFileSystem.list(this).forEach { it.deleteRecursivelyForTest() }
        }
        SystemFileSystem.delete(this)
    }

    @Test
    fun `move copies every file to the new root and removes the old one`() = runTest {
        val appId = "vercors-test-basic"
        val oldRoot = freshDir("basic-old")
        // Named to already end with the appId, so withAppFolderName doesn't append another segment below it.
        val newRoot = Path(freshDir("basic-new-parent"), appId)
        Path(oldRoot, "instances").createDirectories()
        Path(oldRoot, "config.json").writeText("""{"foo":"bar"}""")
        Path(oldRoot, "instances", "note.txt").writeText("hello")

        val appPaths = AppPaths(oldRoot)
        val repository = DataDirectoryRepositoryImpl(appPaths, CryptographyServiceImpl(), appId)

        try {
            either { repository.move(newRoot.toString()) }
                .let { assertTrue(it.isRight(), "expected move to succeed: $it") }

            assertEquals("""{"foo":"bar"}""", Path(newRoot, "config.json").source { it.readString() })
            assertEquals("hello", Path(newRoot, "instances", "note.txt").source { it.readString() })
            assertFalse(oldRoot.exists())
        } finally {
            // move() writes the redirect marker to the real OS-default location: clean it up
            defaultAppRoot(appId).let { if (SystemFileSystem.exists(it)) it.deleteRecursivelyForTest() }
        }
    }

    @Test
    fun `move preserves the redirect marker when the old root is the OS-default location`() = runTest {
        val appId = "vercors-test-marker-regression"
        val oldRoot = defaultAppRoot(appId)
        if (SystemFileSystem.exists(oldRoot)) oldRoot.deleteRecursivelyForTest()
        val newRoot = Path(freshDir("marker-regression-new-parent"), appId)

        try {
            Path(oldRoot, "config.json").writeText("""{"foo":"bar"}""")

            val appPaths = AppPaths(oldRoot)
            val repository = DataDirectoryRepositoryImpl(appPaths, CryptographyServiceImpl(), appId)

            either { repository.move(newRoot.toString()) }
                .let { assertTrue(it.isRight(), "expected move to succeed: $it") }

            assertEquals(
                newRoot.toString(),
                readAppRoot(oldRoot).toString(),
                "the marker written during the move must survive the old root's own cleanup"
            )
        } finally {
            if (SystemFileSystem.exists(oldRoot)) oldRoot.deleteRecursivelyForTest()
        }
    }
}
