package app.vercors.shared.data.game

import app.vercors.meta.client.game.GameApi
import app.vercors.meta.client.game.createGameApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.annotation.Single

@Single
internal fun provideGameApi(ktorfit: Ktorfit): GameApi = ktorfit.createGameApi()
