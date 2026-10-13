package app.vercors.feature.instance.data.repository

import app.vercors.core.cryptography.CryptographyServiceImpl
import app.vercors.core.filesystem.deleteRecursively
import app.vercors.core.filesystem.exists
import app.vercors.core.filesystem.writeText
import app.vercors.shared.data.AppPaths
import arrow.core.Either
import arrow.core.raise.either
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class InstanceExportRepositoryImplJvmTest {
    private val appPaths = AppPaths(Path("build/instance-export-jvm-test"))
    private val repository = InstanceExportRepositoryImpl(appPaths, Json, CryptographyServiceImpl())

    @Test
    fun `hashes and fingerprints only enabled content files`() = runTest {
        val gameDir = appPaths.gameDir("hash")
        if (gameDir.exists()) gameDir.deleteRecursively()
        Path(gameDir, "mods", "example.jar").writeText("jar")
        Path(gameDir, "mods", "old.jar.disabled").writeText("old")
        Path(gameDir, "config", "example.toml").writeText("key = 1")

        val files = (either {
            repository.collectFiles("hash", setOf("mods", "config"), hashContent = true)
        } as Either.Right).value

        assertEquals(
            mapOf(
                "config/example.toml" to null,
                "mods/example.jar" to "f92e777f4341930bad9b2422283c4680d00dbc06",
                "mods/old.jar.disabled" to null
            ),
            files.associate { it.relativePath to it.sha1 }
        )
        assertEquals(
            mapOf("config/example.toml" to null, "mods/example.jar" to 2164523432L, "mods/old.jar.disabled" to null),
            files.associate { it.relativePath to it.curseForgeFingerprint }
        )
    }
}
