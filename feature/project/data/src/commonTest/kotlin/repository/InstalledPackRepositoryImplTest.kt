package app.vercors.feature.project.data.repository

import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.project.domain.model.pack.InstalledPack
import app.vercors.feature.project.domain.model.pack.InstalledPackFile
import app.vercors.shared.data.AppPaths
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import kotlin.test.*

class InstalledPackRepositoryImplTest {
    private val appPaths = AppPaths(Path("build/installed-pack-test"))
    private val repository = InstalledPackRepositoryImpl(Json { ignoreUnknownKeys = true }, appPaths)

    private suspend fun cleanGameDir(instanceId: String): Path =
        appPaths.gameDir(instanceId).also { if (it.exists()) it.deleteRecursively() }

    @Test
    fun `round-trips a saved pack`() = runTest {
        cleanGameDir("round-trip")
        val pack = InstalledPack(
            name = "Friends Pack",
            versionName = "1.2",
            modpackInfo = InstanceModpackInfo("modrinth", "abc", "v1", "1.2"),
            files = listOf(InstalledPackFile("mods/a.jar", "modrinth", "a"), InstalledPackFile("config/a.toml"))
        )
        repository.save("round-trip", pack)
        assertEquals(pack, repository.get("round-trip"))
    }

    @Test
    fun `returns null without a record`() = runTest {
        cleanGameDir("no-record")
        assertNull(repository.get("no-record"))
    }

    @Test
    fun `deletes a game file but never outside the game dir`() = runTest {
        val gameDir = cleanGameDir("delete-file")
        SystemFileSystem.createDirectories(Path(gameDir, "config"))
        SystemFileSystem.sink(Path(gameDir, "config", "a.toml")).close()
        SystemFileSystem.sink(Path(gameDir, "keep.txt")).close()

        repository.deleteGameFile("delete-file", "config/a.toml")
        repository.deleteGameFile("delete-file", "config/../keep.txt")

        assertFalse(Path(gameDir, "config", "a.toml").exists())
        assertTrue(Path(gameDir, "keep.txt").exists())
    }
}
