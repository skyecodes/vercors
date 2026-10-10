package app.vercors.feature.launcher.domain.service

import app.vercors.core.system.OsArch
import app.vercors.core.system.OsInfo
import app.vercors.core.system.OsType
import app.vercors.feature.game.domain.model.GameVersionDetails.Rule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val WINDOWS_10_1803 = Rule.Os.VersionRange(min = "10.0.17134")
private val BEFORE_WINDOWS_10_1803 = Rule.Os.VersionRange(max = "10.0.17134")

private val ZGC_RULES = listOf(
    Rule(action = Rule.Action.Allow, os = Rule.Os(name = "osx")),
    Rule(action = Rule.Action.Allow, os = Rule.Os(name = "linux")),
    Rule(action = Rule.Action.Allow, os = Rule.Os(name = "windows", versionRange = WINDOWS_10_1803))
)
private val G1_RULES = listOf(
    Rule(action = Rule.Action.Allow, os = Rule.Os(name = "windows", versionRange = BEFORE_WINDOWS_10_1803))
)

private fun evaluator(type: OsType, version: String, arch: OsArch = OsArch.X64) =
    LaunchRuleEvaluator(OsInfo(type = type, version = version, arch = arch))

class LaunchRuleEvaluatorTest {
    @Test
    fun `any one of several allow rules matching allows the entry`() {
        assertTrue(evaluator(OsType.Linux, "6.8.1-arch1").isAllowed(ZGC_RULES, emptySet()))
        assertTrue(evaluator(OsType.MacOs, "24.0.0").isAllowed(ZGC_RULES, emptySet()))
    }

    @Test
    fun `no matching rule disallows the entry`() {
        assertFalse(evaluator(OsType.Linux, "6.8.1").isAllowed(G1_RULES, emptySet()))
    }

    @Test
    fun `the last matching rule wins`() {
        val rules = listOf(
            Rule(action = Rule.Action.Allow),
            Rule(action = Rule.Action.Disallow, os = Rule.Os(name = "osx"))
        )
        assertTrue(evaluator(OsType.Linux, "6.8.1").isAllowed(rules, emptySet()))
        assertFalse(evaluator(OsType.MacOs, "24.0.0").isAllowed(rules, emptySet()))
    }

    @Test
    fun `missing or empty rules allow the entry`() {
        assertTrue(evaluator(OsType.Linux, "6.8.1").isAllowed(null, emptySet()))
        assertTrue(evaluator(OsType.Linux, "6.8.1").isAllowed(emptyList(), emptySet()))
    }

    @Test
    fun `versionRange min is inclusive and max is exclusive so exactly one GC is picked`() {
        for (version in listOf("10.0.17134", "10.0.22631.4317")) {
            val windows = evaluator(OsType.Windows, version)
            assertTrue(windows.isAllowed(ZGC_RULES, emptySet()), version)
            assertFalse(windows.isAllowed(G1_RULES, emptySet()), version)
        }
        val oldWindows = evaluator(OsType.Windows, "10.0.17133")
        assertFalse(oldWindows.isAllowed(ZGC_RULES, emptySet()))
        assertTrue(oldWindows.isAllowed(G1_RULES, emptySet()))
    }

    @Test
    fun `versions are compared numerically per segment rather than as strings`() {
        assertTrue(evaluator(OsType.Windows, "10.0.100000").isAllowed(ZGC_RULES, emptySet()))
        assertFalse(evaluator(OsType.Windows, "6.3.9600").isAllowed(ZGC_RULES, emptySet()))
    }

    @Test
    fun `an unknown host version falls back to the rule for older versions`() {
        val windows = evaluator(OsType.Windows, "unknown")
        assertFalse(windows.isAllowed(ZGC_RULES, emptySet()))
        assertTrue(windows.isAllowed(G1_RULES, emptySet()))
    }

    @Test
    fun `feature rules match only when the feature is enabled`() {
        val rules = listOf(Rule(action = Rule.Action.Allow, features = mapOf("is_demo_user" to true)))
        assertFalse(evaluator(OsType.Linux, "6.8.1").isAllowed(rules, emptySet()))
        assertTrue(evaluator(OsType.Linux, "6.8.1").isAllowed(rules, setOf("is_demo_user")))
    }
}
