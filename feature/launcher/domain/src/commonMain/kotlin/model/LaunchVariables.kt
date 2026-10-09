package app.vercors.feature.launcher.domain.model

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance

// Directories are strings to keep the domain layer free of kotlinx.io
data class LaunchVariables(
    val account: Account,
    val instance: Instance,
    val version: GameVersionDetails,
    val classpath: String,
    val gameDir: String,
    val assetsDir: String,
    val nativesDir: String,
    val libraryDir: String,
    val classpathSeparator: String,
    val launcherName: String,
    val launcherVersion: String,
    val quickPlayTarget: QuickPlayTarget? = null
) {
    fun toMap(): Map<String, String> = mapOf(
        "auth_player_name" to account.username,
        "version_name" to instance.gameVersion,
        "game_directory" to gameDir,
        "assets_root" to assetsDir,
        "assets_index_name" to version.assets,
        "auth_uuid" to account.id.toString(),
        "auth_access_token" to account.accessToken,
        "clientid" to "",
        "auth_xuid" to "",
        "user_type" to "msa",
        "version_type" to version.type.mojangId,
        "natives_directory" to nativesDir,
        "launcher_name" to launcherName,
        "launcher_version" to launcherVersion,
        "classpath" to classpath,
        "library_directory" to libraryDir,
        "classpath_separator" to classpathSeparator,
        // Legacy
        "auth_session" to account.accessToken,
        "user_properties" to "{}",
        "uuid" to account.id.toString(),
        "accessToken" to account.accessToken,
        "game_assets" to assetsDir
    ) + when (val target = quickPlayTarget) {
        is QuickPlayTarget.Singleplayer -> mapOf("quickPlaySingleplayer" to target.worldFolderName)
        is QuickPlayTarget.Multiplayer -> mapOf("quickPlayMultiplayer" to target.serverAddress)
        null -> emptyMap()
    }
}
