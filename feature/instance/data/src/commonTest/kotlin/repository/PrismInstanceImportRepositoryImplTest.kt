package app.vercors.feature.instance.data.repository

import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.readText
import app.vercors.core.filesystem.writeText
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.instance.domain.model.localimport.LocalImportError
import app.vercors.shared.data.AppPaths
import arrow.core.Either
import arrow.core.raise.either
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrismInstanceImportRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val appPaths = AppPaths(Path("build/prism-import-test"))
    private val repository = PrismInstanceImportRepositoryImpl(appPaths, json)

    private fun Zip.writeText(entryPath: String, text: String) {
        entryFromSource(Path(entryPath), Buffer().apply { writeString(text) })
    }

    private val forgeMmcPack = """
        {
          "formatVersion": 1,
          "components": [
            {"uid": "net.minecraft", "version": "1.20.1"},
            {"uid": "net.minecraftforge", "version": "47.2.0"}
          ]
        }
    """.trimIndent()

    @Test
    fun `parses a root-level export`() = runTest {
        val path = Path("build/diag-prism-root.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("instance.cfg", "name=My Instance\niconKey=default\n")
            zip.writeText("mmc-pack.json", forgeMmcPack)
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        assertEquals("My Instance", info.name)
        assertEquals("1.20.1", info.gameVersion)
        assertEquals(ModLoaderType.Forge, info.loaderType)
        assertEquals("47.2.0", info.loaderVersion)
        assertEquals("", info.rootPrefix)
        // "default" is a built-in icon key with no corresponding bundled file in this synthetic zip.
        assertNull(info.iconEntryPath)
    }

    @Test
    fun `parses a nested export`() = runTest {
        val path = Path("build/diag-prism-nested.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("MyInstance/instance.cfg", "name=Nested Instance\n")
            zip.writeText("MyInstance/mmc-pack.json", forgeMmcPack)
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        assertEquals("Nested Instance", info.name)
        assertEquals("MyInstance/", info.rootPrefix)
    }

    @Test
    fun `raises NotPrismFormat for a non-Prism zip`() = runTest {
        val path = Path("build/diag-not-prism.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("manifest.json", "{}")
        }
        val result = either { repository.parse(path.toString()) }
        assertEquals(LocalImportError.NotPrismFormat, (result as Either.Left).value)
    }

    @Test
    fun `raises ParseFailed for a malformed mmc-pack`() = runTest {
        val path = Path("build/diag-malformed.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("instance.cfg", "name=Broken\n")
            zip.writeText("mmc-pack.json", "not json")
        }
        val result = either { repository.parse(path.toString()) }
        assertEquals(LocalImportError.ParseFailed, (result as Either.Left).value)
    }

    @Test
    fun `importGameFiles only extracts the minecraft folder`() = runTest {
        val path = Path("build/diag-prism-files.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("instance.cfg", "name=Files Instance\n")
            zip.writeText("mmc-pack.json", forgeMmcPack)
            zip.writeText(".minecraft/mods/foo.jar", "fake-jar-bytes")
            zip.writeText("patches/net.minecraftforge.json", "{}")
            zip.writeText("libraries/somelib.jar", "fake-lib-bytes")
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        val instanceId = "files-instance-test"
        val gameDir = appPaths.gameDir(instanceId)
        if (gameDir.exists()) gameDir.deleteRecursively()

        val result = (either { repository.importGameFiles(path.toString(), instanceId, info) } as Either.Right).value
        assertNull(result.iconPath)
        assertEquals(listOf("mods/foo.jar"), result.extractedPaths)

        assertTrue(Path(gameDir, "mods", "foo.jar").exists())
        assertTrue(!Path(gameDir, "patches").exists())
        assertTrue(!Path(gameDir, "libraries").exists())
    }

    @Test
    fun `importGameFiles keeps existing user state when asked to`() = runTest {
        val path = Path("build/diag-prism-user-state.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("instance.cfg", "name=User State Instance\n")
            zip.writeText("mmc-pack.json", forgeMmcPack)
            zip.writeText(".minecraft/mods/foo.jar", "new-jar")
            zip.writeText(".minecraft/options.txt", "sender-options")
            zip.writeText(".minecraft/saves/World/level.dat", "sender-world")
            zip.writeText(".minecraft/saves/Other/level.dat", "new-world")
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        val instanceId = "user-state-instance-test"
        val gameDir = appPaths.gameDir(instanceId)
        if (gameDir.exists()) gameDir.deleteRecursively()
        Path(gameDir, "options.txt").writeText("my-options")
        Path(gameDir, "saves", "World", "level.dat").writeText("my-world")

        val result = (either {
            repository.importGameFiles(path.toString(), instanceId, info, preserveUserState = true)
        } as Either.Right).value

        assertEquals(listOf("mods/foo.jar"), result.extractedPaths)
        assertEquals("my-options", Path(gameDir, "options.txt").readText())
        assertEquals("my-world", Path(gameDir, "saves", "World", "level.dat").readText())
        assertEquals("new-world", Path(gameDir, "saves", "Other", "level.dat").readText())
        assertEquals("new-jar", Path(gameDir, "mods", "foo.jar").readText())
    }

    @Test
    fun `importGameFiles extracts an undotted minecraft folder`() = runTest {
        // Some Prism exports (e.g. CurseForge-managed packs) name the game folder "minecraft" with no leading dot.
        val path = Path("build/diag-prism-undotted.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("instance.cfg", "name=Undotted Instance\n")
            zip.writeText("mmc-pack.json", forgeMmcPack)
            zip.writeText("minecraft/mods/foo.jar", "fake-jar-bytes")
            zip.writeText("patches/net.minecraftforge.json", "{}")
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        val instanceId = "undotted-instance-test"
        val gameDir = appPaths.gameDir(instanceId)
        if (gameDir.exists()) gameDir.deleteRecursively()

        val result = (either { repository.importGameFiles(path.toString(), instanceId, info) } as Either.Right).value
        assertNull(result.iconPath)

        assertTrue(Path(gameDir, "mods", "foo.jar").exists())
        assertTrue(!Path(gameDir, "patches").exists())
    }

    @Test
    fun `parses CurseForge managed pack info`() = runTest {
        val path = Path("build/diag-prism-managed-curseforge.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText(
                "instance.cfg",
                """
                name=All the Mods 10 - ATM10
                ManagedPack=true
                ManagedPackID=925200
                ManagedPackName=All the Mods 10
                ManagedPackType=flame
                ManagedPackVersionID=7121777
                ManagedPackVersionName=4.14
                """.trimIndent()
            )
            zip.writeText("mmc-pack.json", forgeMmcPack)
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        assertEquals(InstanceModpackInfo("curseforge", "925200", "7121777", "4.14"), info.modpackInfo)
    }

    @Test
    fun `parses Modrinth managed pack info`() = runTest {
        val path = Path("build/diag-prism-managed-modrinth.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText(
                "instance.cfg",
                """
                name=Fabulously Optimized
                ManagedPack=true
                ManagedPackID=1KVo5zza
                ManagedPackType=modrinth
                ManagedPackVersionID=abcd1234
                """.trimIndent()
            )
            zip.writeText("mmc-pack.json", forgeMmcPack)
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        assertEquals(InstanceModpackInfo("modrinth", "1KVo5zza", "abcd1234"), info.modpackInfo)
    }

    @Test
    fun `modpackInfo is null when ManagedPack is false`() = runTest {
        val path = Path("build/diag-prism-not-managed.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText(
                "instance.cfg",
                """
                name=Custom Instance
                ManagedPack=false
                """.trimIndent()
            )
            zip.writeText("mmc-pack.json", forgeMmcPack)
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        assertNull(info.modpackInfo)
    }

    @Test
    fun `modpackInfo is null for an unrecognized managed pack type`() = runTest {
        val path = Path("build/diag-prism-unknown-provider.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText(
                "instance.cfg",
                """
                name=ATLauncher Import
                ManagedPack=true
                ManagedPackID=123
                ManagedPackType=atlauncher
                ManagedPackVersionID=456
                """.trimIndent()
            )
            zip.writeText("mmc-pack.json", forgeMmcPack)
        }
        val info = (either { repository.parse(path.toString()) } as Either.Right).value
        assertNull(info.modpackInfo)
    }
}
