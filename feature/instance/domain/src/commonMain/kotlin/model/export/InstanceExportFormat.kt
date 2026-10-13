package app.vercors.feature.instance.domain.model.export

enum class InstanceExportFormat(val fileExtension: String) {
    Vercors("vcpack"),
    Prism("zip"),
    Modrinth("mrpack")
}
