package app.vercors.shared.data.dto

import app.vercors.shared.domain.model.NativeLibraryOptions
import kotlinx.serialization.Serializable

@Serializable
data class NativeLibraryOptionsData(
    val useSystemGlfw: Boolean = false,
    val glfwPath: String? = null,
    val useSystemOpenAl: Boolean = false,
    val openAlPath: String? = null,
)

fun NativeLibraryOptionsData.toDomain(): NativeLibraryOptions = NativeLibraryOptions(
    useSystemGlfw = useSystemGlfw,
    glfwPath = glfwPath,
    useSystemOpenAl = useSystemOpenAl,
    openAlPath = openAlPath,
)

fun NativeLibraryOptions.toData(): NativeLibraryOptionsData = NativeLibraryOptionsData(
    useSystemGlfw = useSystemGlfw,
    glfwPath = glfwPath,
    useSystemOpenAl = useSystemOpenAl,
    openAlPath = openAlPath,
)
