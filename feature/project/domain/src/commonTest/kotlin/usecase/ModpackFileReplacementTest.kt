package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.project.domain.model.modpack.ModpackFileKind
import app.vercors.feature.project.domain.model.modpack.ModpackManifest
import app.vercors.feature.project.domain.model.modpack.ModpackManifestFile
import kotlin.test.Test
import kotlin.test.assertEquals

class ModpackFileReplacementTest {
    private fun entry(path: String, projectId: String?, kind: ModpackFileKind = ModpackFileKind.Mod) =
        ModpackManifestFile(
            targetPath = path,
            kind = kind,
            downloadUrl = null,
            sha1 = null,
            size = null,
            providerProjectId = projectId,
            providerFileId = null
        )

    private fun manifest(vararg files: ModpackManifestFile) = ModpackManifest(
        platformId = "modrinth",
        projectId = "pack",
        fileId = "v",
        name = "Pack",
        gameVersion = "1.20.1",
        loaderType = null,
        loaderVersion = null,
        files = files.toList(),
        overridesDirs = emptyList()
    )

    @Test
    fun `removes files dropped by the new version and keeps unchanged ones`() {
        val old = manifest(entry("mods/kept.jar", "a"), entry("mods/dropped.jar", "b"))
        val new = manifest(entry("mods/kept.jar", "a"))
        val installed = mapOf(ContentKind.Mod to mapOf("kept.jar" to true, "dropped.jar" to true))

        val plan = planModpackFileReplacement(old, new, installed)

        assertEquals(listOf(ContentKind.Mod to "dropped.jar"), plan.removed)
        assertEquals(emptyList(), plan.disabled)
    }

    @Test
    fun `never touches files the user added`() {
        val old = manifest(entry("mods/pack.jar", "a"))
        val new = manifest()
        val installed = mapOf(ContentKind.Mod to mapOf("pack.jar" to true, "mine.jar" to true))

        val plan = planModpackFileReplacement(old, new, installed)

        assertEquals(listOf(ContentKind.Mod to "pack.jar"), plan.removed)
    }

    @Test
    fun `skips files of the old version that are already gone`() {
        val old = manifest(entry("mods/deleted.jar", "a"))
        val plan = planModpackFileReplacement(old, manifest(), mapOf(ContentKind.Mod to emptyMap()))

        assertEquals(emptyList(), plan.removed)
    }

    @Test
    fun `removes disabled files and disables the replacement of the same project`() {
        val oldFile = entry("mods/mod-1.0.jar", "a")
        val old = manifest(oldFile, entry("mods/other.jar", "b"))
        val new = manifest(entry("mods/mod-2.0.jar", "a"), entry("mods/other.jar", "b"))
        val installed = mapOf(ContentKind.Mod to mapOf("mod-1.0.jar" to false, "other.jar" to true))

        val plan = planModpackFileReplacement(old, new, installed)

        assertEquals(listOf(ContentKind.Mod to "mod-1.0.jar"), plan.removed)
        assertEquals(listOf(oldFile), plan.disabled)
        assertEquals(listOf("mods/mod-2.0.jar"), new.filesToDisable(plan.disabled).map { it.targetPath })
    }

    @Test
    fun `ignores override files`() {
        val old = manifest(entry("config/x.toml", null, ModpackFileKind.Other))
        val plan = planModpackFileReplacement(old, manifest(), mapOf(ContentKind.Mod to mapOf("x.toml" to true)))

        assertEquals(emptyList(), plan.removed)
    }
}
