package app.vercors.feature.instance.domain.model

/**
 * The last modpack-install pipeline stage this instance reached before being interrupted (launcher closed, crash,
 * etc.), persisted on [Instance.installStage] so a restart can tell "install never finished" apart from "fully
 * installed" and offer to resume. Purely a display/reconciliation marker — resuming always re-runs the whole
 * pipeline from the top, it never resumes from this exact stage.
 */
enum class InstanceInstallStage {
    ResolvingFiles, DownloadingFiles
}
