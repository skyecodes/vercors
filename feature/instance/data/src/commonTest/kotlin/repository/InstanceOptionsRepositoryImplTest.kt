package app.vercors.feature.instance.data.repository

import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.source
import app.vercors.core.filesystem.writeText
import app.vercors.feature.instance.data.dto.InstanceData
import app.vercors.feature.instance.domain.model.options.*
import app.vercors.shared.data.AppPaths
import arrow.core.Either
import arrow.core.raise.either
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlin.test.*
import kotlin.time.Instant

class InstanceOptionsRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val appPaths = AppPaths(Path("build/instance-options-test"))

    private suspend fun freshRepository(): InstanceOptionsRepositoryImpl {
        if (appPaths.root.exists()) appPaths.root.deleteRecursively()
        return InstanceOptionsRepositoryImpl(appPaths, json)
    }

    private fun <T> Either<*, T>.value(): T = (this as Either.Right).value

    private suspend fun Path.text(): String = source { it.readString() }

    @Test
    fun `reads and writes options txt through the game directory`() = runTest {
        val repository = freshRepository()
        val source = OptionsSource.Instance("a")
        assertNull(either { repository.readOptions(source) }.value())
        val options = GameOptions(3953, mapOf("fov" to "0.5", "key_key.jump" to "key.keyboard.space"))
        either { repository.writeOptions(source, options) }.value()
        assertEquals(options, either { repository.readOptions(source) }.value())
        assertFalse(Path(appPaths.gameDir("a"), "options.txt.tmp").exists())
    }

    @Test
    fun `copies extra files only when absent unless overwriting`() = runTest {
        val repository = freshRepository()
        Path(appPaths.gameDir("a"), "servers.dat").writeText("source")
        Path(appPaths.gameDir("b"), "servers.dat").writeText("target")
        val from = OptionsSource.Instance("a")
        val to = OptionsSource.Instance("b")
        either { repository.copyFile(from, to, OptionsFile.ServersDat, overwrite = false) }.value()
        assertEquals("target", Path(appPaths.gameDir("b"), "servers.dat").text())
        either { repository.copyFile(from, to, OptionsFile.ServersDat, overwrite = true) }.value()
        assertEquals("source", Path(appPaths.gameDir("b"), "servers.dat").text())
        either { repository.copyFile(from, to, OptionsFile.HotbarNbt, overwrite = true) }.value()
        assertFalse(Path(appPaths.gameDir("b"), "hotbar.nbt").exists())
    }

    @Test
    fun `copies referenced resource packs without overwriting existing ones`() = runTest {
        val repository = freshRepository()
        val sourcePacks = Path(appPaths.gameDir("a"), "resourcepacks")
        Path(sourcePacks, "Faithful.zip").writeText("faithful")
        Path(sourcePacks, "Folder Pack", "pack.mcmeta").writeText("{}")
        Path(sourcePacks, "Unused.zip").writeText("unused")
        Path(appPaths.gameDir("b"), "resourcepacks", "Faithful.zip").writeText("existing")
        val options = GameOptions(
            null,
            mapOf(
                "resourcePacks" to """["vanilla","file/Faithful.zip","file/Folder Pack","file/../escape.zip"]""",
                "incompatibleResourcePacks" to "[]"
            )
        )
        either {
            repository.copyResourcePacks(
                OptionsSource.Instance("a"),
                OptionsSource.Instance("b"),
                options
            )
        }.value()
        val targetPacks = Path(appPaths.gameDir("b"), "resourcepacks")
        assertEquals("existing", Path(targetPacks, "Faithful.zip").text())
        assertEquals("{}", Path(targetPacks, "Folder Pack", "pack.mcmeta").text())
        assertFalse(Path(targetPacks, "Unused.zip").exists())
    }

    @Test
    fun `saves and clears the default options template`() = runTest {
        val repository = freshRepository()
        assertNull(repository.getDefaultOptions())
        val defaultOptions = DefaultOptions(
            categories = setOf(OptionsCategory.Keybinds, OptionsCategory.ServerList),
            sourceInstanceName = "Main",
            gameVersion = "1.21.1",
            capturedAt = Instant.parse("2026-10-15T12:00:00Z"),
        )
        either { repository.saveDefaultOptions(defaultOptions) }.value()
        assertEquals(defaultOptions, InstanceOptionsRepositoryImpl(appPaths, json).getDefaultOptions())
        either { repository.clearDefaultOptions() }.value()
        assertNull(repository.getDefaultOptions())
        assertFalse(appPaths.defaultOptions.exists())
    }

    @Test
    fun `instances saved before default options existed count as already applied`() {
        val data = json.decodeFromString<InstanceData>(
            """{"name":"Old","gameVersion":"1.20.1","modLoader":null,"modpackInfo":null,
               "createdAt":"2026-01-01T00:00:00Z","updatedAt":"2026-01-01T00:00:00Z","lastPlayedAt":null,"playTime":"PT0S"}"""
        )
        assertTrue(data.defaultOptionsApplied)
    }
}
