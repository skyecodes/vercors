package app.vercors.shared.domain.model

data class NativeLibraryOptions(
    val useSystemGlfw: Boolean? = null,
    val glfwPath: String? = null,
    val useSystemOpenAl: Boolean? = null,
    val openAlPath: String? = null,
) {
    /** Per-field inheritance: any `null` field here falls back to the corresponding field in [global]. */
    fun withFallback(global: NativeLibraryOptions): NativeLibraryOptions = NativeLibraryOptions(
        useSystemGlfw = useSystemGlfw ?: global.useSystemGlfw,
        glfwPath = glfwPath ?: global.glfwPath,
        useSystemOpenAl = useSystemOpenAl ?: global.useSystemOpenAl,
        openAlPath = openAlPath ?: global.openAlPath,
    )
}
