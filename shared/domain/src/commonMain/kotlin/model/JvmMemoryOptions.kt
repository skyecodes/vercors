package app.vercors.shared.domain.model

data class JvmMemoryOptions(
    val minMemoryEnabled: Boolean? = null,
    val minMemoryMb: Int? = null,
    val maxMemoryEnabled: Boolean? = null,
    val maxMemoryMb: Int? = null,
    val extraArgs: String? = null,
) {
    /** Per-field inheritance: any `null` field here falls back to the corresponding field in [global].
     *  [minMemoryEnabled]/[maxMemoryEnabled] additionally fall back to "a concrete value is present on
     *  either side" when neither this instance nor [global] set them explicitly — this keeps
     *  pre-existing persisted overrides (created before these enabled flags existed) behaving exactly as
     *  before: a plain value with no enabled flag still emits its JVM arg. */
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
