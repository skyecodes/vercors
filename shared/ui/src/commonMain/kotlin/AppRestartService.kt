package app.vercors.shared.ui

/** Restarts the whole launcher process (as opposed to [SnackbarService] etc.'s in-app-only concerns). Its only
 *  real implementation lives in `app/launcher-gui` — the only module that depends on Nucleus. */
fun interface AppRestartService {
    fun restart()
}
