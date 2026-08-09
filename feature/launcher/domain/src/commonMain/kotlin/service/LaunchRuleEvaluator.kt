package app.vercors.feature.launcher.domain.service

import app.vercors.core.system.OsInfo
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionDetails.Argument
import app.vercors.feature.game.domain.model.GameVersionDetails.Rule
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import dev.zacsweers.metro.Inject

/**
 * Evaluates the platform- and feature-conditional rules attached to a Mojang version manifest.
 *
 * Takes the host identity as an [OsInfo] value rather than a `SystemService`, so it can be
 * evaluated against any platform without touching the machine it runs on.
 */
@Inject
class LaunchRuleEvaluator(private val hostOs: OsInfo) {
    /** Drops the libraries and arguments that do not apply to [hostOs] and the enabled [features]. */
    fun applyRules(
        version: GameVersionDetails,
        features: Collection<InstanceLaunchFeature>
    ): GameVersionDetails {
        val enabled = features.mapTo(mutableSetOf()) { it.id }
        return version.copy(
            libraries = version.libraries.filter { isAllowed(it.rules, enabled) },
            jvmArgs = version.jvmArgs?.filter { isAllowed(it, enabled) },
            gameArgs = version.gameArgs?.filter { isAllowed(it, enabled) }
        )
    }

    fun isAllowed(rules: List<Rule>?, enabledFeatures: Set<String>): Boolean =
        rules == null || rules.all { it.isAllowed(enabledFeatures) }

    fun isAllowed(argument: Argument, enabledFeatures: Set<String>): Boolean = when (argument) {
        is Argument.Basic -> true
        is Argument.Conditional -> argument.rules.all { it.isAllowed(enabledFeatures) }
    }

    private fun Rule.isAllowed(enabledFeatures: Set<String>): Boolean {
        val applies = featuresMatch(features, enabledFeatures) && matchesHostOs(os)
        return when (action) {
            Rule.Action.Allow -> applies
            Rule.Action.Disallow -> !applies
        }
    }

    private fun featuresMatch(required: Map<String, Boolean>?, enabled: Set<String>): Boolean =
        required == null || required.all { (feature, expected) -> (feature in enabled) == expected }

    private fun matchesHostOs(required: Rule.Os?): Boolean {
        if (required == null) return true
        val name = required.name
        if (!name.isNullOrEmpty() && hostOs.type.id != name) return false
        val arch = required.arch
        if (!arch.isNullOrEmpty() && hostOs.arch.id != arch) return false
        val version = required.version
        return version.isNullOrEmpty() || version.toRegex().containsMatchIn(hostOs.version)
    }
}
