package app.vercors.feature.launcher.domain.model

import app.vercors.feature.account.domain.model.Account
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.instance.domain.model.Instance
import app.vercors.shared.domain.LAUNCHER_NAME
import app.vercors.shared.domain.LAUNCHER_VERSION

/**
 * The substitution variables that Mojang's argument templates are resolved against.
 *
 * Directories are taken as strings rather than paths so this stays independent of any filesystem
 * API, keeping the domain layer free of `kotlinx.io`.
 */
data class LaunchVariables(
    val account: Account,
    val instance: Instance,
    val version: GameVersionDetails,
    val classpath: String,
    val gameDir: String,
    val assetsDir: String,
    val nativesDir: String,
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
        "launcher_name" to LAUNCHER_NAME,
        "launcher_version" to LAUNCHER_VERSION,
        "classpath" to classpath,
        // Legacy
        "auth_session" to account.accessToken,
        "user_properties" to "{}",
        "uuid" to account.id.toString(),
        "accessToken" to account.accessToken,
        "game_assets" to assetsDir
    )
}
