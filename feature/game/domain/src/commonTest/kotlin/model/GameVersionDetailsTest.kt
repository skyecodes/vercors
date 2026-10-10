package app.vercors.feature.game.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

private val VANILLA = GameVersionDetails(
    id = "1.7.10",
    type = GameVersionType.Release,
    clientDownload = GameVersionDetails.Download(
        sha1 = "vanilla-sha1",
        size = 1,
        url = "https://example.invalid/client.jar"
    ),
    serverDownload = null,
    libraries = emptyList(),
    javaVersion = null,
    gameArgs = listOf(
        GameVersionDetails.Argument.Basic("--username"),
        GameVersionDetails.Argument.Basic($$"${auth_player_name}")
    ),
    jvmArgs = null,
    defaultUserJvmArgs = null,
    clientLogging = null,
    mainClass = "net.minecraft.client.main.Main",
    assets = "1.7.10",
    assetIndex = GameVersionDetails.AssetIndex(
        id = "1.7.10",
        sha1 = "index-sha1",
        size = 1,
        totalSize = 1,
        url = "https://example.invalid/index.json"
    )
)

class GameVersionDetailsTest {
    @Test
    fun `a modern loader profile's gameArgs are appended after the vanilla ones`() {
        val profile = LoaderInstallProfile(
            mainClass = "cpw.mods.modlauncher.Launcher",
            libraries = emptyList(),
            gameArgs = listOf(
                GameVersionDetails.Argument.Basic("--fml.forgeVersion"),
                GameVersionDetails.Argument.Basic("47.2.0")
            ),
            jvmArgs = null
        )
        val merged = VANILLA.mergeWith(profile)
        assertEquals(
            listOf("--username", $$"${auth_player_name}", "--fml.forgeVersion", "47.2.0"),
            merged.gameArgs?.map { (it as GameVersionDetails.Argument.Basic).value }
        )
    }

    @Test
    fun `a legacy Forge profile's gameArgs replace the vanilla ones instead of being appended`() {
        val profile = LoaderInstallProfile(
            mainClass = "net.minecraft.launchwrapper.Launch",
            libraries = emptyList(),
            gameArgs = listOf(
                GameVersionDetails.Argument.Basic("--username"),
                GameVersionDetails.Argument.Basic("--tweakClass")
            ),
            jvmArgs = null,
            replacesGameArgs = true
        )
        val merged = VANILLA.mergeWith(profile)
        assertEquals(
            listOf("--username", "--tweakClass"),
            merged.gameArgs?.map { (it as GameVersionDetails.Argument.Basic).value }
        )
    }
}
