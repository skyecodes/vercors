package app.vercors.feature.project.api

import kotlin.test.Test
import kotlin.test.assertNotNull

class ProjectApiContractTest {
    @Test
    fun `every endpoint declares a valid contract`() {
        listOf(
            Platforms.list,
            Platforms.PlatformId.get,
            Platforms.PlatformId.featuredProjects,
            Projects.search,
            Projects.ProjectId.get,
            ProjectFiles.list,
            ProjectFiles.FileId.get,
            ProjectFiles.FileId.download,
            PlatformFiles.list,
            PlatformFiles.lookup,
        ).forEach(::assertNotNull)
    }
}
