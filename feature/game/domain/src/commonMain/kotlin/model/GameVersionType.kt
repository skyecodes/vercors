package app.vercors.feature.game.domain.model

enum class GameVersionType(val mojangId: String) {
    Release("release"),
    Snapshot("snapshot"),
    Beta("old_beta"),
    Alpha("old_alpha")
}
