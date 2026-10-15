package app.vercors.feature.project.domain.model.update

import app.vercors.feature.game.domain.model.ModLoaderType
import app.vercors.feature.project.domain.model.ProjectFile
import app.vercors.feature.project.domain.model.ProjectReleaseChannel
import kotlin.test.*
import kotlin.time.Instant

class ProjectFileUpdateUtilsTest {
    private fun file(
        id: String,
        publishedAt: Long,
        channel: ProjectReleaseChannel = ProjectReleaseChannel.Release,
        loaders: List<ModLoaderType> = emptyList(),
    ) =
        ProjectFile(
            id = id,
            displayName = id,
            fileName = "$id.jar",
            gameVersions = emptyList(),
            loaders = loaders,
            releaseChannel = channel,
            size = 1,
            sha1 = null,
            publishedAt = Instant.fromEpochSeconds(publishedAt),
            dependencies = emptyList()
        )

    @Test
    fun `release accepts only release`() {
        assertTrue(ProjectReleaseChannel.Release.accepts(ProjectReleaseChannel.Release))
        assertFalse(ProjectReleaseChannel.Release.accepts(ProjectReleaseChannel.Beta))
        assertFalse(ProjectReleaseChannel.Release.accepts(ProjectReleaseChannel.Alpha))
    }

    @Test
    fun `beta accepts release and beta but not alpha`() {
        assertTrue(ProjectReleaseChannel.Beta.accepts(ProjectReleaseChannel.Release))
        assertTrue(ProjectReleaseChannel.Beta.accepts(ProjectReleaseChannel.Beta))
        assertFalse(ProjectReleaseChannel.Beta.accepts(ProjectReleaseChannel.Alpha))
    }

    @Test
    fun `alpha accepts every channel`() {
        ProjectReleaseChannel.entries.forEach { assertTrue(ProjectReleaseChannel.Alpha.accepts(it)) }
    }

    @Test
    fun `finds no update when nothing newer exists`() {
        val current = file("current", 10)
        assertNull(listOf(file("older", 5), current).findUpdateFor(current))
    }

    @Test
    fun `ignores newer files on a less stable channel`() {
        val current = file("current", 10)
        val files = listOf(current, file("beta", 20, ProjectReleaseChannel.Beta))
        assertNull(files.findUpdateFor(current))
    }

    @Test
    fun `picks the newest release over a newer beta`() {
        val current = file("current", 10, ProjectReleaseChannel.Beta)
        val files = listOf(
            current,
            file("release-1", 15),
            file("release-2", 20),
            file("beta", 30, ProjectReleaseChannel.Beta)
        )
        assertEquals("release-2", files.findUpdateFor(current)?.id)
    }

    @Test
    fun `falls back to the newest accepted pre-release`() {
        val current = file("current", 10, ProjectReleaseChannel.Alpha)
        val files = listOf(
            current,
            file("alpha", 20, ProjectReleaseChannel.Alpha),
            file("beta", 15, ProjectReleaseChannel.Beta)
        )
        assertEquals("alpha", files.findUpdateFor(current)?.id)
    }

    @Test
    fun `a file without a supported loader runs on nothing`() {
        assertFalse(file("bukkit", 10).runsOn(ModLoaderType.Fabric))
    }

    @Test
    fun `quilt runs fabric files but not the reverse`() {
        assertTrue(file("fabric", 10, loaders = listOf(ModLoaderType.Fabric)).runsOn(ModLoaderType.Quilt))
        assertFalse(file("quilt", 10, loaders = listOf(ModLoaderType.Quilt)).runsOn(ModLoaderType.Fabric))
    }
}
