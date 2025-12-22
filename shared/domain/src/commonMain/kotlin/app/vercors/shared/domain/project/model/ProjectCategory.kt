package app.vercors.shared.domain.project.model

data class ProjectCategory(
    val id: String,
    val slug: String,
    val name: String?,
    val icon: Icon?
) {
    sealed interface Icon {
        data class Named(val name: String) : Icon
        data class Data(val svgString: String) : Icon
    }
}
