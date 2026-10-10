package app.vercors.feature.config.domain

import app.vercors.feature.config.domain.model.UpdateChannel
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateChannelTest {
    @Test
    fun `maps release versions to the stable channel`() {
        assertEquals(UpdateChannel.Stable, UpdateChannel.fromVersion("1.2.0"))
    }

    @Test
    fun `maps beta and alpha pre-releases to their channel`() {
        assertEquals(UpdateChannel.Beta, UpdateChannel.fromVersion("1.3.0-beta.2"))
        assertEquals(UpdateChannel.Alpha, UpdateChannel.fromVersion("1.3.0-alpha"))
    }

    @Test
    fun `maps unknown pre-release labels to the stable channel`() {
        assertEquals(UpdateChannel.Stable, UpdateChannel.fromVersion("1.3.0-rc.1"))
    }
}
