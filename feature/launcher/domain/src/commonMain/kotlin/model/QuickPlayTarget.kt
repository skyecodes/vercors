package app.vercors.feature.launcher.domain.model

/** What to jump straight into on launch, via Minecraft's "Quick Play" argument features. */
sealed interface QuickPlayTarget {
    data class Singleplayer(val worldFolderName: String) : QuickPlayTarget
    data class Multiplayer(val serverAddress: String) : QuickPlayTarget
}
