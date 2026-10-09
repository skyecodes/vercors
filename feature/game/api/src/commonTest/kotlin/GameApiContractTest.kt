package app.vercors.feature.game.api

import kotlin.test.Test
import kotlin.test.assertNotNull

class GameApiContractTest {
    @Test
    fun `every endpoint declares a valid contract`() {
        listOf(
            Game.versions,
            Game.runtimes,
            Loaders.list,
            Loaders.LoaderId.versions,
            Loaders.LoaderId.installUrl,
        ).forEach(::assertNotNull)
    }
}
