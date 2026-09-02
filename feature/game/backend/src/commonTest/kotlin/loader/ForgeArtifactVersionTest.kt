package app.vercors.feature.game.backend.loader

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Real entries observed on maven.minecraftforge.net's
// /api/maven/versions/releases/net%2Fminecraftforge%2Fforge for MC 1.7.10.
private val REAL_1_7_10_RAW_VERSIONS = listOf(
    "1.7.10-10.13.0.1150",
    "1.7.10-10.13.2.1300-1.7.10",
    "1.7.10-10.13.3.1389-1710ls",
    "1.7.10-10.13.4.1614-1.7.10",
)

class ForgeArtifactVersionTest {
    @Test
    fun `matches a bare build number for an unsuffixed build`() {
        assertEquals(
            "1.7.10-10.13.0.1150",
            resolveForgeArtifactVersion("1.7.10", "10.13.0.1150", REAL_1_7_10_RAW_VERSIONS)
        )
    }

    @Test
    fun `matches a bare build number against a build suffixed with the game version`() {
        // Real build from the bug report: naive concatenation 404s, the true artifact 200s.
        assertEquals(
            "1.7.10-10.13.4.1614-1.7.10",
            resolveForgeArtifactVersion("1.7.10", "10.13.4.1614", REAL_1_7_10_RAW_VERSIONS)
        )
    }

    @Test
    fun `matches a bare build number against a build with a non-numeric qualifier suffix`() {
        assertEquals(
            "1.7.10-10.13.3.1389-1710ls",
            resolveForgeArtifactVersion("1.7.10", "10.13.3.1389", REAL_1_7_10_RAW_VERSIONS)
        )
    }

    @Test
    fun `also matches the full raw suffixed remainder verbatim`() {
        assertEquals(
            "1.7.10-10.13.4.1614-1.7.10",
            resolveForgeArtifactVersion("1.7.10", "10.13.4.1614-1.7.10", REAL_1_7_10_RAW_VERSIONS)
        )
    }

    @Test
    fun `returns null for an unknown build`() {
        assertNull(resolveForgeArtifactVersion("1.7.10", "99.99.99.9999", REAL_1_7_10_RAW_VERSIONS))
    }
}
