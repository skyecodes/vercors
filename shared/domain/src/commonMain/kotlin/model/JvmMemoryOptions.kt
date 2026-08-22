package app.vercors.shared.domain.model

data class JvmMemoryOptions(
    val minMemoryMb: Int? = null,
    val maxMemoryMb: Int? = null,
    val extraArgs: String = "",
)
