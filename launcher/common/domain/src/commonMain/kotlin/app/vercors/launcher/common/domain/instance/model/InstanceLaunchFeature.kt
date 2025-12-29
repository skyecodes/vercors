package app.vercors.launcher.common.domain.instance.model

enum class InstanceLaunchFeature(val id: String) {
    IsDemoUser("is_demo_user"),
    HasCustomResolution("has_custom_resolution"),
    HasQuickPlaysSupport("has_quick_plays_support"),
    IsQuickPlaySingleplayer("is_quick_play_singleplayer"),
    IsQuickPlayMultiplayer("is_quick_play_multiplayer"),
    IsQuickPlayRealms("is_quick_play_realms"),
}
