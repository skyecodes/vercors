package app.vercors.feature.instance.data.localimport

import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.localimport.LocalImportNotPrismFormat
import app.vercors.feature.instance.domain.model.localimport.LocalImportParseFailed
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
    fun parsesRootLevelExport() = runTest {
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
    fun parsesNestedExport() = runTest {
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
    fun raisesNotPrismFormatForNonPrismZip() = runTest {
        val path = Path("build/diag-not-prism.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("manifest.json", "{}")
        }
        val result = either { repository.parse(path.toString()) }
        assertEquals(LocalImportNotPrismFormat, (result as Either.Left).value)
    }

    @Test
    fun raisesParseFailedForMalformedMmcPack() = runTest {
        val path = Path("build/diag-malformed.zip")
        Zip.open(path, mode = Zip.Mode.Write).use { zip ->
            zip.writeText("instance.cfg", "name=Broken\n")
            zip.writeText("mmc-pack.json", "not json")
        }
        val result = either { repository.parse(path.toString()) }
        assertEquals(LocalImportParseFailed, (result as Either.Left).value)
    }

    @Test
    fun importGameFilesOnlyExtractsMinecraftFolder() = runTest {
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

        val iconPath = (either { repository.importGameFiles(path.toString(), instanceId, info) } as Either.Right).value
        assertNull(iconPath)

        assertTrue(Path(gameDir, "mods", "foo.jar").exists())
        assertTrue(!Path(gameDir, "patches").exists())
        assertTrue(!Path(gameDir, "libraries").exists())
    }
}
