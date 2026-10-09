package app.vercors.feature.launcher.domain.model

sealed interface QuickPlayTarget {
    data class Singleplayer(val worldFolderName: String) : QuickPlayTarget
    data class Multiplayer(val serverAddress: String) : QuickPlayTarget
}
