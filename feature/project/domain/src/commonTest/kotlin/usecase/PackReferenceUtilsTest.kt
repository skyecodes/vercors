package app.vercors.feature.project.domain.usecase

import app.vercors.feature.instance.domain.model.ModProvider
import app.vercors.feature.instance.domain.model.export.InstanceExportFile
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectFileMatch
import app.vercors.feature.project.domain.model.ProjectReleaseChannel
import app.vercors.feature.project.domain.model.from
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class PackReferenceUtilsTest {
    private val file = InstanceExportFile("mods/example.jar", "/game/mods/example.jar", 10, "sha1-a", 42)

    private fun projectFile(
        id: String = "file-1",
        sha1: String? = "sha1-a",
        sha512: String? = "sha512-a",
        downloadUrl: String? = "https://cdn.modrinth.com/data/p/versions/v/example.jar",
        manualDownloadUrl: String? = null
    ) = ProjectFile(
        id = id,
        displayName = "Example",
        fileName = "example.jar",
        gameVersions = emptyList(),
        loaders = emptyList(),
        releaseChannel = ProjectReleaseChannel.Release,
        size = 10,
        sha1 = sha1,
        sha512 = sha512,
        downloadUrl = downloadUrl,
        publishedAt = Instant.fromEpochSeconds(0),
        dependencies = emptyList(),
        manualDownloadUrl = manualDownloadUrl
    )

    private val provider = ModProvider("curseforge", "123", "file-1")

    @Test
    fun `keeps a recorded origin whose remote file has the same hash`() {
        val references =
            selectVerifiedReferences("curseforge", listOf(file to provider), mapOf("file-1" to projectFile()))
        assertEquals(listOf("mods/example.jar"), references.map { it.relativePath })
        assertEquals("123", references.single().projectId)
    }

    @Test
    fun `drops a recorded origin whose remote hash differs`() {
        val remote = mapOf("file-1" to projectFile(sha1 = "sha1-other"))
        assertEquals(emptyList(), selectVerifiedReferences("curseforge", listOf(file to provider), remote))
    }

    @Test
    fun `drops a recorded origin that needs a manual download`() {
        val remote = mapOf("file-1" to projectFile(manualDownloadUrl = "https://www.curseforge.com/download"))
        assertEquals(emptyList(), selectVerifiedReferences("curseforge", listOf(file to provider), remote))
    }

    @Test
    fun `drops a recorded origin the platform no longer knows`() {
        assertEquals(emptyList(), selectVerifiedReferences("curseforge", listOf(file to provider), emptyMap()))
    }

    @Test
    fun `references a Modrinth hash match`() {
        val matches = mapOf("sha1-a" to ProjectFileMatch("sha1-a", "p" from "modrinth", projectFile(id = "v")))
        val reference = selectHashMatchReferences("modrinth", listOf(file), matches, requireMrpackHost = true).single()
        assertEquals("modrinth", reference.platformId)
        assertEquals("p", reference.projectId)
        assertEquals("v", reference.fileId)
        assertEquals("sha512-a", reference.sha512)
    }

    @Test
    fun `skips mrpack matches hosted outside the allowed domains`() {
        val match =
            ProjectFileMatch("sha1-a", "p" from "modrinth", projectFile(downloadUrl = "https://example.com/a.jar"))
        val matches = mapOf("sha1-a" to match)
        assertEquals(
            emptyList(),
            selectHashMatchReferences("modrinth", listOf(file), matches, requireMrpackHost = true)
        )
        assertEquals(1, selectHashMatchReferences("modrinth", listOf(file), matches, requireMrpackHost = false).size)
    }

    @Test
    fun `skips mrpack matches without a sha512`() {
        val matches = mapOf("sha1-a" to ProjectFileMatch("sha1-a", "p" from "modrinth", projectFile(sha512 = null)))
        assertEquals(
            emptyList(),
            selectHashMatchReferences("modrinth", listOf(file), matches, requireMrpackHost = true)
        )
    }

    @Test
    fun `references a CurseForge hash match under its own platform`() {
        val matches =
            mapOf("sha1-a" to ProjectFileMatch("sha1-a", "238222" from "curseforge", projectFile(id = "4567")))
        val reference =
            selectHashMatchReferences("curseforge", listOf(file), matches, requireMrpackHost = false).single()
        assertEquals("curseforge", reference.platformId)
        assertEquals("238222", reference.projectId)
        assertEquals("4567", reference.fileId)
    }

    @Test
    fun `skips a hash match that needs a manual download`() {
        val remote = projectFile(manualDownloadUrl = "https://www.curseforge.com/minecraft/mc-mods/x/download/1")
        val matches = mapOf("sha1-a" to ProjectFileMatch("sha1-a", "238222" from "curseforge", remote))
        assertEquals(
            emptyList(),
            selectHashMatchReferences("curseforge", listOf(file), matches, requireMrpackHost = false)
        )
    }

    @Test
    fun `ignores files without a hash`() {
        val unhashed = file.copy(sha1 = null)
        val matches = mapOf("sha1-a" to ProjectFileMatch("sha1-a", "p" from "modrinth", projectFile()))
        assertEquals(
            emptyList(),
            selectHashMatchReferences("modrinth", listOf(unhashed), matches, requireMrpackHost = false)
        )
    }
}
