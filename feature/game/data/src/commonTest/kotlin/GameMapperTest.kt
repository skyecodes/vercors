package app.vercors.feature.game.data

import app.vercors.feature.game.domain.model.GameVersionDetails
import kotlin.test.Test
import kotlin.test.assertEquals

class GameMapperTest {
    @Test
    fun `tokenizes a legacy minecraftArguments string into positional Basic arguments`() {
        // Real "minecraftArguments" value from forge-1.7.10-10.13.4.1614-1.7.10-installer.jar's versionInfo.
        val minecraftArguments =
            "--username \${auth_player_name} --version \${version_name} --gameDir \${game_directory}" +
                    " --tweakClass cpw.mods.fml.common.launcher.FMLTweaker"

        val args = minecraftArguments.toBasicArguments()

        assertEquals(
            listOf(
                "--username", "\${auth_player_name}",
                "--version", "\${version_name}",
                "--gameDir", "\${game_directory}",
                "--tweakClass", "cpw.mods.fml.common.launcher.FMLTweaker"
            ).map { GameVersionDetails.Argument.Basic(it) },
            args
        )
    }

    @Test
    fun `collapses repeated whitespace and trims`() {
        val args = "  --username   foo  ".toBasicArguments()
        assertEquals(
            listOf(GameVersionDetails.Argument.Basic("--username"), GameVersionDetails.Argument.Basic("foo")),
            args
        )
    }
}
