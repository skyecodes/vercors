package app.vercors.shared.domain.model

data class JvmMemoryOptions(
    val minMemoryMb: Int? = null,
    val maxMemoryMb: Int? = null,
    val extraArgs: String? = null,
) {
    /** Per-field inheritance: any `null` field here falls back to the corresponding field in [global]. */
    fun withFallback(global: JvmMemoryOptions): JvmMemoryOptions = JvmMemoryOptions(
        minMemoryMb = minMemoryMb ?: global.minMemoryMb,
        maxMemoryMb = maxMemoryMb ?: global.maxMemoryMb,
        extraArgs = extraArgs ?: global.extraArgs,
    )
}
