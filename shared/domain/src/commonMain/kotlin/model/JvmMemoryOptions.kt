package app.vercors.shared.domain.model

data class JvmMemoryOptions(
    val minMemoryEnabled: Boolean? = null,
    val minMemoryMb: Int? = null,
    val maxMemoryEnabled: Boolean? = null,
    val maxMemoryMb: Int? = null,
    val extraArgs: String? = null,
) {
    // Enabled flags fall back to "a value is set on either side", for overrides saved before the flags existed
    fun withFallback(global: JvmMemoryOptions): JvmMemoryOptions {
        val minEnabled = minMemoryEnabled
            ?: global.minMemoryEnabled
            ?: (minMemoryMb != null || global.minMemoryMb != null)
        val maxEnabled = maxMemoryEnabled
            ?: global.maxMemoryEnabled
            ?: (maxMemoryMb != null || global.maxMemoryMb != null)
        return JvmMemoryOptions(
            minMemoryEnabled = minEnabled,
            minMemoryMb = minMemoryMb ?: global.minMemoryMb,
            maxMemoryEnabled = maxEnabled,
            maxMemoryMb = maxMemoryMb ?: global.maxMemoryMb,
            extraArgs = extraArgs ?: global.extraArgs,
        )
    }
}
