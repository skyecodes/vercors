package app.vercors.feature.config.data.repository

import app.vercors.core.cryptography.CryptographyServiceImpl
import app.vercors.core.filesystem.*
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.ContentCacheKind
import arrow.core.raise.context.either
import kotlinx.coroutines.test.runTest
import kotlinx.io.bytestring.toHexString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.io.path.Path as JvmPath

class DataDirectoryRepositoryImplJvmTest {
    private suspend fun freshDir(name: String): Path {
        val dir = Path("build/data-directory-repository-jvm-test/$name")
        if (SystemFileSystem.exists(dir)) dir.deleteRecursively()
        return dir
    }

    private fun Path.createParentDirectoriesForTest() {
        parent?.let { SystemFileSystem.createDirectories(it) }
    }

    private fun isSameFile(a: Path, b: Path): Boolean = Files.isSameFile(JvmPath(a.toString()), JvmPath(b.toString()))

    @Test
    fun `move re-links cache-backed mod files instead of copying them`() = runTest {
        val appId = "vercors-test-hardlink"
        val oldRoot = freshDir("hardlink-old")
        val newRoot = Path(freshDir("hardlink-new-parent"), appId)
        val appPaths = AppPaths(oldRoot)
        val cryptographyService = CryptographyServiceImpl()

        val modFile = Path(appPaths.modsDir("instance1"), "example.jar")
        modFile.createParentDirectoriesForTest()
        modFile.writeText("pretend this is a jar")
        val sha1 = modFile.source { cryptographyService.sha1(it) }.toHexString()
        val cacheObject = appPaths.contentCacheObjectFile(ContentCacheKind.Mod, sha1)
        cacheObject.createParentDirectoriesForTest()
        assertTrue(modFile.linkOrCopyTo(cacheObject))
        assertTrue(isSameFile(modFile, cacheObject), "precondition: source mod and cache object are the same file")

        val repository = DataDirectoryRepositoryImpl(appPaths, cryptographyService, appId)
        try {
            either { repository.move(newRoot.toString()) }.let {
                assertTrue(
                    it.isRight(),
                    "expected move to succeed: $it"
                )
            }

            val newAppPaths = AppPaths(newRoot)
            val newModFile = Path(newAppPaths.modsDir("instance1"), "example.jar")
            val newCacheObject = newAppPaths.contentCacheObjectFile(ContentCacheKind.Mod, sha1)
            assertTrue(newModFile.exists())
            assertTrue(newCacheObject.exists())
            assertTrue(
                isSameFile(newCacheObject, newModFile),
                "moved mod file should be re-linked to the moved cache object, not an independent copy"
            )
        } finally {
            // move() writes the redirect marker to the real OS-default location: clean it up
            defaultAppRoot(appId).let { if (SystemFileSystem.exists(it)) it.deleteRecursively() }
        }
    }
}
