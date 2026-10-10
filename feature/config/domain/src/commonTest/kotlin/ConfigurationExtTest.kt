package app.vercors.feature.config.domain

import app.vercors.feature.config.domain.model.Configuration
import app.vercors.feature.config.domain.model.withoutJavaInstallations
import app.vercors.shared.domain.model.JavaInstallation
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigurationExtTest {
    private fun java(id: String, majorVersion: Int) = JavaInstallation(
        id = id,
        path = id,
        majorVersion = majorVersion,
        versionString = "$majorVersion",
        vendor = "vendor",
        arch = "x64",
    )

    private val java17a = java("17a", 17)
    private val java17b = java("17b", 17)
    private val java21 = java("21", 21)

    private val configuration = Configuration.Default.copy(
        javaInstallations = listOf(java17a, java17b, java21),
        preferredJavaByMajorVersion = mapOf(17 to "17a", 21 to "21"),
    )

    @Test
    fun `reassigns the preferred installation to another one with the same major version`() {
        val result = configuration.withoutJavaInstallations(setOf("17a"))
        assertEquals(listOf(java17b, java21), result.javaInstallations)
        assertEquals(mapOf(17 to "17b", 21 to "21"), result.preferredJavaByMajorVersion)
    }

    @Test
    fun `drops the preferred entry when no installation of that major version remains`() {
        val result = configuration.withoutJavaInstallations(setOf("21"))
        assertEquals(mapOf(17 to "17a"), result.preferredJavaByMajorVersion)
    }

    @Test
    fun `keeps the preferred installation when a non-preferred one is removed`() {
        val result = configuration.withoutJavaInstallations(setOf("17b"))
        assertEquals(listOf(java17a, java21), result.javaInstallations)
        assertEquals(configuration.preferredJavaByMajorVersion, result.preferredJavaByMajorVersion)
    }

    @Test
    fun `drops the preferred entry when every installation of that major version is removed`() {
        val result = configuration.withoutJavaInstallations(setOf("17a", "17b"))
        assertEquals(listOf(java21), result.javaInstallations)
        assertEquals(mapOf(21 to "21"), result.preferredJavaByMajorVersion)
    }
}
