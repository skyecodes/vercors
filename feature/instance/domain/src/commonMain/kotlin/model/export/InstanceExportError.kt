package app.vercors.feature.instance.domain.model.export

sealed interface InstanceExportError {
    data object ReadFailed : InstanceExportError
    data object WriteFailed : InstanceExportError
}
