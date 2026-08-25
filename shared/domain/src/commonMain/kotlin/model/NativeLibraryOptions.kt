package app.vercors.shared.domain.model

data class NativeLibraryOptions(
    val useSystemGlfw: Boolean = false,
    val glfwPath: String? = null,
    val useSystemOpenAl: Boolean = false,
    val openAlPath: String? = null,
)
