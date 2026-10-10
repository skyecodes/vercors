package app.vercors.feature.game.data.dto

import app.vercors.feature.game.data.toDomain
import app.vercors.feature.game.domain.model.GameVersionDetails
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

private val json = Json { ignoreUnknownKeys = true }

// Real 26.4-snapshot-3.json, libraries and logging stripped
private val REAL_SNAPSHOT_VERSION = $$"""
{"arguments":{"default-user-jvm":[{"value":["-Xms2G","-Xmx4G","-XX:+UseCompactObjectHeaders","-XX:+AlwaysPreTouch","-XX:+UseStringDeduplication"]},{"rules":[{"action":"allow","os":{"name":"osx"}},{"action":"allow","os":{"name":"linux"}},{"action":"allow","os":{"name":"windows","versionRange":{"min":"10.0.17134"}}}],"value":["-XX:+UseZGC"]},{"rules":[{"action":"allow","os":{"name":"windows","versionRange":{"max":"10.0.17134"}}}],"value":["-XX:+UnlockExperimentalVMOptions","-XX:+UseG1GC","-XX:G1NewSizePercent=20","-XX:G1ReservePercent=20","-XX:MaxGCPauseMillis=50","-XX:G1HeapRegionSize=32M"]}],"game":["--username","${auth_player_name}","--version","${version_name}","--gameDir","${game_directory}","--assetsDir","${assets_root}","--assetIndex","${assets_index_name}","--uuid","${auth_uuid}","--accessToken","${auth_access_token}","--clientId","${clientid}","--xuid","${auth_xuid}","--versionType","${version_type}",{"rules":[{"action":"allow","features":{"is_demo_user":true}}],"value":"--demo"},{"rules":[{"action":"allow","features":{"has_custom_resolution":true}}],"value":["--width","${resolution_width}","--height","${resolution_height}"]},{"rules":[{"action":"allow","features":{"has_quick_plays_support":true}}],"value":["--quickPlayPath","${quickPlayPath}"]},{"rules":[{"action":"allow","features":{"is_quick_play_singleplayer":true}}],"value":["--quickPlaySingleplayer","${quickPlaySingleplayer}"]},{"rules":[{"action":"allow","features":{"is_quick_play_multiplayer":true}}],"value":["--quickPlayMultiplayer","${quickPlayMultiplayer}"]},{"rules":[{"action":"allow","features":{"is_quick_play_realms":true}}],"value":["--quickPlayRealms","${quickPlayRealms}"]}],"jvm":[{"rules":[{"action":"allow","os":{"name":"osx"}}],"value":["-XstartOnFirstThread"]},{"rules":[{"action":"allow","os":{"name":"windows"}}],"value":"-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"},{"rules":[{"action":"allow","os":{"arch":"x86"}}],"value":"-Xss1M"},"-XX:StackShadowPages=32","--enable-native-access=ALL-UNNAMED","--add-exports","java.base/jdk.internal.misc=ALL-UNNAMED","-Djava.library.path=${natives_directory}/java","-Djna.tmpdir=${natives_directory}/jna","-Dorg.lwjgl.system.SharedLibraryExtractPath=${natives_directory}/lwjgl","-Dio.netty.native.workdir=${natives_directory}/netty","-Dminecraft.launcher.brand=${launcher_name}","-Dminecraft.launcher.version=${launcher_version}","-cp","${classpath}"]},"assetIndex":{"id":"36","sha1":"8b18bfed19e4218ed7e453ed86b2fb4dea3f864b","size":606244,"totalSize":475778007,"url":"https://piston-meta.mojang.com/v1/packages/8b18bfed19e4218ed7e453ed86b2fb4dea3f864b/36.json"},"assets":"36","complianceLevel":1,"downloads":{"client":{"sha1":"96611d3cbefeae43527552fd008a7031b6f46e71","size":41918883,"url":"https://piston-data.mojang.com/v1/objects/96611d3cbefeae43527552fd008a7031b6f46e71/client.jar"},"server":{"sha1":"2d89c95c030e635387448f332961074ce1adbb4b","size":62458029,"url":"https://piston-data.mojang.com/v1/objects/2d89c95c030e635387448f332961074ce1adbb4b/server.jar"}},"id":"26.4-snapshot-3","javaVersion":{"component":"java-runtime-epsilon","majorVersion":25},"libraries":[],"mainClass":"net.minecraft.client.main.Main","minimumLauncherVersion":21,"releaseTime":"2026-10-06T12:54:26+00:00","time":"2026-10-06T12:54:26+00:00","type":"snapshot"}
"""

class MojangVersionInfoTest {
    private val version = json.decodeFromString(MojangVersionInfo.serializer(), REAL_SNAPSHOT_VERSION).toDomain()

    @Test
    fun `decodes default-user-jvm arguments including entries without rules`() {
        val defaults = version.defaultUserJvmArgs!!
        assertEquals(3, defaults.size)
        val unconditional = defaults[0] as GameVersionDetails.Argument.Conditional
        assertEquals(emptyList(), unconditional.rules)
        assertEquals("-Xms2G", unconditional.value.first())
    }

    @Test
    fun `decodes os versionRange bounds on rules`() {
        val defaults = version.defaultUserJvmArgs!!
        val zgc = defaults[1] as GameVersionDetails.Argument.Conditional
        assertEquals(
            GameVersionDetails.Rule.Os.VersionRange(min = "10.0.17134"),
            zgc.rules.last().os?.versionRange
        )
        val g1 = defaults[2] as GameVersionDetails.Argument.Conditional
        assertEquals(
            GameVersionDetails.Rule.Os.VersionRange(max = "10.0.17134"),
            g1.rules.single().os?.versionRange
        )
    }
}
