package app.vercors.feature.instance.domain.model.export

data class ExportableEntry(
    val name: String,
    val isDirectory: Boolean,
    val includedByDefault: Boolean
)
