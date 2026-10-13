package app.vercors.feature.project.data.repository

import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.writeText
import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.feature.instance.domain.model.InstanceModLoader
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.project.data.modpack.ModrinthModpackParser
import app.vercors.feature.project.domain.model.ProjectId
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind
import app.vercors.feature.project.domain.model.pack.PackFileReference
import app.vercors.feature.project.domain.model.pack.VercorsPackError
import app.vercors.shared.data.AppPaths
import app.vercors.shared.data.toZipEntryName
import app.vercors.shared.data.writeZipAtomically
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InstancePackRepositoryImplTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val dir = Path("build/instance-pack-test")
    private val repository = InstancePackRepositoryImpl(json, AppPaths(dir))

    private val instance = Instance(
        name = "Pack Test",
        id = "pack-test",
        gameVersion = "1.20.1",
        modLoader = InstanceModLoader(ModLoaderType.Fabric, "0.15.0"),
        jvmMemoryOptions = JvmMemoryOptions(maxMemoryEnabled = true, maxMemoryMb = 6144),
        nativeLibraryOptions = NativeLibraryOptions(useSystemGlfw = true, glfwPath = "/usr/lib/libglfw.so")
    )

    private val reference = PackFileReference(
        relativePath = "mods/sodium.jar",
        platformId = "modrinth",
        projectId = "AANobbMI",
        fileId = "abc123",
        sha1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709",
        sha512 = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce",
        size = 42,
        downloadUrl = "https://cdn.modrinth.com/data/AANobbMI/versions/abc123/sodium.jar"
    )

    private suspend fun overrideFile(): InstanceExportFile {
        val source = Path(dir, "source", "options.txt")
        if (!source.exists()) source.writeText("fov:70")
        return InstanceExportFile("options.txt", source.toString(), 6, null, null)
    }

    private fun entryNames(path: Path): List<String> {
        val names = mutableListOf<String>()
        Zip.open(path).use { zip -> zip.forEachEntry { names += it.path.toZipEntryName() } }
        return names
    }

    @Test
    fun `writes an mrpack the Modrinth parser reads back`() = runTest {
        val target = Path(dir, "export.mrpack")
        val result = either {
            repository.writeMrpack(instance, listOf(reference), listOf(overrideFile()), "2.0.0", "", target.toString())
            ModrinthModpackParser(json).parse(target, ProjectId("local", "modrinth"), "local")
        }
        val manifest = (result as Either.Right).value
        assertEquals("Pack Test", manifest.name)
        assertEquals("1.20.1", manifest.gameVersion)
        assertEquals(ModLoaderType.Fabric, manifest.loaderType)
        assertEquals("0.15.0", manifest.loaderVersion)
        val file = manifest.files.single()
        assertEquals("mods/sodium.jar", file.targetPath)
        assertEquals(ModpackFileKind.Mod, file.kind)
        assertEquals("AANobbMI", file.providerProjectId)
        assertEquals("abc123", file.providerFileId)
        assertEquals(42, file.size)
        assertEquals(listOf("modrinth.index.json", "overrides/options.txt"), entryNames(target))
    }

    @Test
    fun `writes a vcpack that parses back with settings and references`() = runTest {
        val target = Path(dir, "export.vcpack")
        val info = (either {
            repository.writeVercorsPack(instance, listOf(reference), listOf(overrideFile()), target.toString())
            repository.parseVercorsPack(target.toString())
        } as Either.Right).value
        assertEquals("Pack Test", info.name)
        assertEquals(instance.modLoader, info.modLoader)
        assertEquals(instance.jvmMemoryOptions, info.jvmMemoryOptions)
        assertEquals(true, info.nativeLibraryOptions.useSystemGlfw)
        assertNull(info.nativeLibraryOptions.glfwPath)
        assertEquals(listOf("overrides"), info.manifest.overridesDirs)
        val file = info.manifest.files.single()
        assertEquals("mods/sodium.jar", file.targetPath)
        assertEquals("modrinth", file.platformId)
        assertNull(file.downloadUrl)
        assertEquals(reference.sha1, file.sha1)
    }

    @Test
    fun `writes a fully bundled vcpack`() = runTest {
        val target = Path(dir, "bundled.vcpack")
        val info = (either {
            repository.writeVercorsPack(instance, emptyList(), listOf(overrideFile()), target.toString())
            repository.parseVercorsPack(target.toString())
        } as Either.Right).value
        assertTrue(info.manifest.files.isEmpty())
        assertTrue("overrides/options.txt" in entryNames(target))
    }

    @Test
    fun `rejects a zip without a vercors manifest`() = runTest {
        val target = Path(dir, "other.zip")
        target.writeZipAtomically { addBytes("instance.cfg", "name=x".encodeToByteArray()) }
        val result = either { repository.parseVercorsPack(target.toString()) }
        assertEquals(VercorsPackError.NotVercorsFormat, (result as Either.Left).value)
    }

    @Test
    fun `rejects file paths escaping the game directory`() = runTest {
        val target = Path(dir, "escape.vcpack")
        val manifest = """
            {"formatVersion": 1, "name": "x", "gameVersion": "1.20.1",
             "files": [{"path": "../evil.jar", "platformId": "modrinth", "projectId": "p", "fileId": "f", "sha1": "s", "size": 1}]}
        """.trimIndent()
        target.writeZipAtomically { addBytes("vercors.json", manifest.encodeToByteArray()) }
        val result = either { repository.parseVercorsPack(target.toString()) }
        assertEquals(VercorsPackError.ParseFailed, (result as Either.Left).value)
    }

    @Test
    fun `rejects a newer format version`() = runTest {
        val target = Path(dir, "future.vcpack")
        target.writeZipAtomically {
            addBytes(
                "vercors.json",
                """{"formatVersion": 2, "name": "x", "gameVersion": "1.20.1"}""".encodeToByteArray()
            )
        }
        val result = either { repository.parseVercorsPack(target.toString()) }
        assertEquals(VercorsPackError.UnsupportedFormatVersion, (result as Either.Left).value)
    }
}
