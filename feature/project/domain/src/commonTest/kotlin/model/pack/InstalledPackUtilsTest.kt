package app.vercors.feature.project.domain.model.pack

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.model.InstanceModpackInfo
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackManifestFile
import app.vercors.shared.domain.model.JvmMemoryOptions
import app.vercors.shared.domain.model.NativeLibraryOptions
import kotlin.test.*

class InstalledPackUtilsTest {
    private fun pack(
        vararg files: InstalledPackFile,
        name: String = "Pack",
        modpackInfo: InstanceModpackInfo? = null
    ) =
        InstalledPack(name = name, versionName = null, modpackInfo = modpackInfo, files = files.toList())

    private fun mrpack(name: String) = LocalPack.Mrpack(
        sourcePath = "pack.mrpack",
        manifest = ModpackManifest(
            platformId = "modrinth",
            projectId = "",
            fileId = "",
            name = name,
            gameVersion = "1.21.1",
            loaderType = null,
            loaderVersion = null,
            files = emptyList(),
            overridesDirs = emptyList()
        )
    )

    @Test
    fun `removes files the new pack dropped and keeps the rest`() {
        val old = pack(
            InstalledPackFile("mods/a.jar"),
            InstalledPackFile("mods/b.jar"),
            InstalledPackFile("config/a.toml")
        )
        val new = pack(InstalledPackFile("mods/b.jar"), InstalledPackFile("mods/c.jar"))
        assertEquals(listOf("mods/a.jar", "config/a.toml"), planPackFileReplacement(old, new, emptySet()).removed)
    }

    @Test
    fun `removes nothing without a previous record`() {
        val new = pack(InstalledPackFile("mods/b.jar"))
        assertTrue(planPackFileReplacement(null, new, emptySet()).removed.isEmpty())
    }

    @Test
    fun `never removes user state even if an old record listed it`() {
        val old = pack(InstalledPackFile("options.txt"), InstalledPackFile("saves/World/level.dat"))
        assertTrue(planPackFileReplacement(old, pack(), emptySet()).removed.isEmpty())
    }

    @Test
    fun `keeps a disabled mod disabled across a version bump`() {
        val old = pack(InstalledPackFile("mods/sodium-1.jar", "modrinth", "AANobbMI"))
        val new = pack(InstalledPackFile("mods/sodium-2.jar", "modrinth", "AANobbMI"))
        val plan = planPackFileReplacement(old, new, disabledOldPaths = setOf("mods/sodium-1.jar"))
        assertEquals(listOf("mods/sodium-1.jar"), plan.removed)
        assertEquals(listOf(PackContentFile(ContentKind.Mod, "sodium-2.jar")), plan.toDisable)
    }

    @Test
    fun `keeps a disabled file without a project disabled when its name is unchanged`() {
        val old = pack(InstalledPackFile("resourcepacks/pack.zip"))
        val new = pack(InstalledPackFile("resourcepacks/pack.zip"))
        val plan = planPackFileReplacement(old, new, disabledOldPaths = setOf("resourcepacks/pack.zip"))
        assertTrue(plan.removed.isEmpty())
        assertEquals(listOf(PackContentFile(ContentKind.ResourcePack, "pack.zip")), plan.toDisable)
    }

    @Test
    fun `only direct children of a content folder are content`() {
        assertEquals(PackContentFile(ContentKind.ShaderPack, "a.zip"), packContentFileOrNull("shaderpacks/a.zip"))
        assertNull(packContentFileOrNull("mods/sub/a.jar"))
        assertNull(packContentFileOrNull("config/a.toml"))
        assertNull(packContentFileOrNull("mods"))
    }

    @Test
    fun `matches a pack by name when either side has no platform info`() {
        assertTrue(pack(name = "Friends Pack").matches(mrpack(" friends pack ")))
        assertFalse(pack(name = "Friends Pack").matches(mrpack("Other Pack")))
    }

    @Test
    fun `matches a pack by platform project when both sides have one`() {
        val info = InstanceModpackInfo("modrinth", "abc", "v1")
        val renamed = LocalPack.Vercors(
            sourcePath = "pack.vcpack",
            info = VercorsPackInfo(
                name = "Renamed",
                gameVersion = "1.21.1",
                modLoader = null,
                modpackInfo = info.copy(fileId = "v2"),
                jvmMemoryOptions = JvmMemoryOptions(),
                nativeLibraryOptions = NativeLibraryOptions(),
                iconExtension = null,
                manifest = mrpack("Renamed").manifest
            )
        )
        assertTrue(pack(name = "Original", modpackInfo = info).matches(renamed))
    }

    @Test
    fun `records manifest files with their project and overrides without`() {
        val manifest = mrpack("Pack").manifest.copy(
            files = listOf(
                ModpackManifestFile(
                    targetPath = "mods/a.jar",
                    kind = ModpackFileKind.Mod,
                    downloadUrl = null,
                    sha1 = null,
                    size = null,
                    providerProjectId = "a",
                    providerFileId = "1"
                )
            )
        )
        val installed = manifest.toInstalledPack("Pack", null, listOf("config/a.toml", "options.txt", "mods/a.jar"))
        assertEquals(
            listOf(InstalledPackFile("mods/a.jar", "modrinth", "a"), InstalledPackFile("config/a.toml")),
            installed.files
        )
    }
}
