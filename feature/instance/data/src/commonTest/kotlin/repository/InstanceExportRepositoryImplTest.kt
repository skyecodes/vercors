package app.vercors.feature.instance.data.repository

import app.vercors.core.cryptography.CryptographyServiceImpl
import app.vercors.core.filesystem.createDirectories
import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.writeText
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.toZipEntryName
import app.vercors.shared.domain.model.JvmMemoryOptions
import arrow.core.Either
import arrow.core.raise.either
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.forEachEntry
import de.jonasbroeckmann.kzip.open
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstanceExportRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val appPaths = AppPaths(Path("build/instance-export-test"))
    private val repository = InstanceExportRepositoryImpl(appPaths, json, CryptographyServiceImpl())
    private val importRepository = PrismInstanceImportRepositoryImpl(appPaths, json)

    private suspend fun setUpInstance(id: String) {
        val gameDir = appPaths.gameDir(id)
        if (gameDir.exists()) gameDir.deleteRecursively()
        Path(gameDir, "mods", "example.jar").writeText("jar")
        Path(gameDir, "mods", "old.jar.disabled").writeText("old")
        Path(gameDir, "config", "nested", "example.toml").writeText("key = 1")
        Path(gameDir, "logs", "latest.log").writeText("log")
        Path(gameDir, "natives").createDirectories()
        Path(gameDir, "options.txt").writeText("fov:70")
        Path(gameDir, "icon.png").writeText("png")
    }

    @Test
    fun `lists top-level entries without natives or the icon`() = runTest {
        setUpInstance("listing")
        val entries = repository.listExportableEntries("listing")
        assertEquals(listOf("config", "logs", "mods", "options.txt"), entries.map { it.name })
        assertEquals(listOf(true, false, true, true), entries.map { it.includedByDefault })
    }

    @Test
    fun `collects selected files in a stable order`() = runTest {
        setUpInstance("collect")
        val files = (either {
            repository.collectFiles("collect", setOf("mods", "config", "natives"), hashContent = false)
        } as Either.Right).value
        assertEquals(
            listOf("config/nested/example.toml", "mods/example.jar", "mods/old.jar.disabled"),
            files.map { it.relativePath }
        )
        assertTrue(files.all { it.sha1 == null })
    }

    @Test
    fun `writes a Prism export the Prism importer reads back`() = runTest {
        setUpInstance("prism")
        val instance = Instance(
            name = "Create; Above & Beyond, Again",
            id = "prism",
            gameVersion = "1.20.1",
            modLoader = InstanceModLoader(ModLoaderType.Fabric, "0.15.0"),
            modpackInfo = InstanceModpackInfo("curseforge", "123", "456"),
            iconFilePath = appPaths.instanceIconFile("prism", "png").toString(),
            jvmMemoryOptions = JvmMemoryOptions(maxMemoryEnabled = true, maxMemoryMb = 4096)
        )
        val target = Path("build/instance-export-test/prism.zip")
        val result = either {
            val files = repository.collectFiles("prism", setOf("mods", "config"), hashContent = false)
            repository.writePrismExport(instance, files, target.toString())
            importRepository.parse(target.toString())
        }
        val info = (result as Either.Right).value
        assertEquals(instance.name, info.name)
        assertEquals("1.20.1", info.gameVersion)
        assertEquals(ModLoaderType.Fabric, info.loaderType)
        assertEquals("0.15.0", info.loaderVersion)
        assertEquals(instance.modpackInfo, info.modpackInfo)
        assertEquals("vercors_prism.png", info.iconEntryPath)

        val entryNames = mutableListOf<String>()
        var cfg = ""
        Zip.open(target).use { zip ->
            zip.forEachEntry { entry ->
                val name = entry.path.toZipEntryName()
                entryNames += name
                if (name == "instance.cfg") cfg = entry.readToBytes().decodeToString()
            }
        }
        assertTrue(".minecraft/config/nested/example.toml" in entryNames)
        assertTrue(".minecraft/mods/example.jar" in entryNames)
        assertTrue(entryNames.none { it.startsWith(".minecraft/logs") })
        assertTrue("OverrideMemory=true" in cfg)
        assertTrue("MaxMemAlloc=4096" in cfg)
        assertTrue("MinMemAlloc" !in cfg)
    }
}
