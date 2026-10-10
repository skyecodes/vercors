package app.vercors.feature.launcher.domain.service

import app.vercors.core.system.OsInfo
import app.vercors.feature.game.domain.model.GameVersionDetails
import app.vercors.feature.game.domain.model.GameVersionDetails.Argument
import app.vercors.feature.game.domain.model.GameVersionDetails.Rule
import app.vercors.feature.launcher.domain.model.InstanceLaunchFeature
import dev.zacsweers.metro.Inject

@Inject
class LaunchRuleEvaluator(private val hostOs: OsInfo) {
    fun applyRules(
        version: GameVersionDetails,
        features: Collection<InstanceLaunchFeature>
    ): GameVersionDetails {
        val enabled = features.mapTo(mutableSetOf()) { it.id }
        return version.copy(
            libraries = version.libraries.filter { isAllowed(it.rules, enabled) },
            jvmArgs = version.jvmArgs?.filter { isAllowed(it, enabled) },
            defaultUserJvmArgs = version.defaultUserJvmArgs?.filter { isAllowed(it, enabled) },
            gameArgs = version.gameArgs?.filter { isAllowed(it, enabled) }
        )
    }

    // Mojang semantics: disallowed by default, and the last rule whose conditions match decides
    fun isAllowed(rules: List<Rule>?, enabledFeatures: Set<String>): Boolean =
        rules.isNullOrEmpty() || rules.fold(false) { allowed, rule ->
            if (rule.matches(enabledFeatures)) rule.action == Rule.Action.Allow else allowed
        }

    fun isAllowed(argument: Argument, enabledFeatures: Set<String>): Boolean = when (argument) {
        is Argument.Basic -> true
        is Argument.Conditional -> isAllowed(argument.rules, enabledFeatures)
    }

    private fun Rule.matches(enabledFeatures: Set<String>): Boolean =
        featuresMatch(features, enabledFeatures) && matchesHostOs(os)

    private fun featuresMatch(required: Map<String, Boolean>?, enabled: Set<String>): Boolean =
        required == null || required.all { (feature, expected) -> (feature in enabled) == expected }

    private fun matchesHostOs(required: Rule.Os?): Boolean {
        if (required == null) return true
        val name = required.name
        if (!name.isNullOrEmpty() && hostOs.type.id != name) return false
        val arch = required.arch
        if (!arch.isNullOrEmpty() && hostOs.arch.id != arch) return false
        val version = required.version
        return !(!version.isNullOrEmpty() && !version.toRegex().containsMatchIn(hostOs.version)) && required.versionRange?.contains(hostOs.version) ?: true
    }

    private fun Rule.Os.VersionRange.contains(version: String): Boolean {
        val min = min
        val max = max
        return (min == null || compareVersions(version, min) >= 0) && (max == null || compareVersions(version, max) < 0)
    }

    private fun compareVersions(left: String, right: String): Int {
        val leftSegments = left.versionSegments()
        val rightSegments = right.versionSegments()
        for (i in 0 until maxOf(leftSegments.size, rightSegments.size)) {
            val comparison = leftSegments.getOrElse(i) { 0 }.compareTo(rightSegments.getOrElse(i) { 0 })
            if (comparison != 0) return comparison
        }
        return 0
    }

    private fun String.versionSegments(): List<Long> =
        split('.').map { segment -> segment.takeWhile(Char::isDigit).toLongOrNull() ?: 0 }
}
