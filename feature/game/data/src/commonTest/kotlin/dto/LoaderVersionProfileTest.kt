package app.vercors.feature.game.data.dto

import app.vercors.feature.game.data.toDomain
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val json = Json { ignoreUnknownKeys = true }

private const val REAL_FABRIC_PROFILE = """
{"id":"fabric-loader-0.15.11-1.20.1","inheritsFrom":"1.20.1","releaseTime":"2026-08-16T03:31:04+0000","time":"2026-08-16T03:31:04+0000","type":"release","mainClass":"net.fabricmc.loader.impl.launch.knot.KnotClient","arguments":{"game":[],"jvm":["-DFabricMcEmu= net.minecraft.client.main.Main "]},"libraries":[{"name":"org.ow2.asm:asm:9.6","url":"https://maven.fabricmc.net/","md5":"6f8bccf756f170d4185bb24c8c2d2020","sha1":"aa205cf0a06dbd8e04ece91c0b37c3f5d567546a","sha256":"3c6fac2424db3d4a853b669f4e3d1d9c3c552235e19a319673f887083c2303a1","sha512":"01a5ea6f5b43bf094c52a50e18325a60af7bb02e74d24f9bc2c727d43e514578fd968b30ff22f9d2720caec071458f9ff82d11a21fbb1ebc42d8203e737c4b52","size":123598},{"name":"net.fabricmc:intermediary:1.20.1","url":"https://maven.fabricmc.net/"},{"name":"net.fabricmc:fabric-loader:0.15.11","url":"https://maven.fabricmc.net/"}]}
"""

// Real meta.quiltmc.org response: note "arguments" omits "jvm" entirely, unlike Fabric's/vanilla's schema.
private const val REAL_QUILT_PROFILE = """
{"id":"quilt-loader-0.23.1-1.20.1","inheritsFrom":"1.20.1","type":"release","mainClass":"org.quiltmc.loader.impl.launch.knot.KnotClient","arguments":{"game":[]},"libraries":[{"name":"net.fabricmc:tiny-mappings-parser:0.3.0+build.17","url":"https://maven.fabricmc.net/"},{"name":"org.quiltmc:quilt-loader:0.23.1","url":"https://maven.quiltmc.org/repository/release/"}],"releaseTime":"2023-06-12T13:25:51+00:00","time":"2026-07-28T06:43:40+00:00"}
"""

class LoaderVersionProfileTest {
    @Test
    fun `real Fabric loader profile keeps every library artifact non-null so it isn't dropped from the classpath`() {
        val profile = json.decodeFromString(FabricLoaderProfile.serializer(), REAL_FABRIC_PROFILE).toDomain()
        assertEquals("net.fabricmc.loader.impl.launch.knot.KnotClient", profile.mainClass)
        assertEquals(3, profile.libraries.size)
        for (library in profile.libraries) {
            assertNotNull(library.artifact, "expected an artifact for ${library.name}")
        }
        val fabricLoader = profile.libraries.single { it.name == "net.fabricmc:fabric-loader:0.15.11" }
        assertEquals(
            "net/fabricmc/fabric-loader/0.15.11/fabric-loader-0.15.11.jar",
            fabricLoader.artifact?.path
        )
        assertEquals(
            "https://maven.fabricmc.net/net/fabricmc/fabric-loader/0.15.11/fabric-loader-0.15.11.jar",
            fabricLoader.artifact?.url
        )
    }

    @Test
    fun `real Quilt loader profile parses despite omitting jvm arguments`() {
        val profile = json.decodeFromString(FabricLoaderProfile.serializer(), REAL_QUILT_PROFILE).toDomain()
        assertEquals("org.quiltmc.loader.impl.launch.knot.KnotClient", profile.mainClass)
        assertEquals(2, profile.libraries.size)
        for (library in profile.libraries) {
            assertNotNull(library.artifact, "expected an artifact for ${library.name}")
        }
    }
}
