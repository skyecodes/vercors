package app.vercors.shared.ui

// Only implemented in app/launcher-gui, the only module depending on Nucleus
fun interface AppRestartService {
    fun restart()
}
