package app.vercors.feature.game.data.loader

import kotlin.test.Test
import kotlin.test.assertEquals

class MavenCoordinateTest {
    @Test
    fun `converts a classifier-less coordinate`() {
        // Real "PATCHED" data value from a NeoForge 26.2.0.59 install_profile.json.
        assertEquals(
            "net/neoforged/minecraft-client-patched/26.2.0.59/minecraft-client-patched-26.2.0.59.jar",
            mavenCoordinateToPath("net.neoforged:minecraft-client-patched:26.2.0.59")
        )
    }

    @Test
    fun `converts a coordinate with a classifier`() {
        // Real "PATCHED" data value from a NeoForge 21.1.209 install_profile.json.
        assertEquals(
            "net/neoforged/neoforge/21.1.209/neoforge-21.1.209-client.jar",
            mavenCoordinateToPath("net.neoforged:neoforge:21.1.209:client")
        )
    }

    @Test
    fun `converts a coordinate with an explicit extension`() {
        assertEquals(
            "de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/mcp_config-1.20.1-20230612.114412-mappings.txt",
            mavenCoordinateToPath("de.oceanlabs.mcp:mcp_config:1.20.1-20230612.114412:mappings@txt")
        )
    }
}
