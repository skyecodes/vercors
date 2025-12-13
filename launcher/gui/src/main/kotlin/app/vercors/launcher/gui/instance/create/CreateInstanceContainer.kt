package app.vercors.launcher.gui.instance.create

import app.vercors.launcher.common.domain.instance.usecase.CreateInstanceUseCase
import app.vercors.launcher.common.domain.loader.ModLoaderType
import app.vercors.shared.domain.Result
import app.vercors.shared.domain.game.model.GameVersionList
import app.vercors.shared.domain.game.usecase.GetGameVersionListUseCase
import app.vercors.shared.ui.configureWithLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import pro.respawn.flowmvi.api.*
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.init
import pro.respawn.flowmvi.plugins.reduce

private val logger = KotlinLogging.logger {}

class CreateInstanceContainer(
    private val getMinecraftVersionList: GetGameVersionListUseCase,
    private val createInstance: CreateInstanceUseCase
) : Container<CreateInstanceState, CreateInstanceIntent, CreateInstanceAction> {
    override val store: Store<CreateInstanceState, CreateInstanceIntent, CreateInstanceAction> =
        store(CreateInstanceState()) {
            configureWithLogger(logger)

            reduce { intent ->
                when (intent) {
                    else -> {}
                }
            }

            init {
                val versionList = getMinecraftVersionList()
                updateState {
                    copy(gameVersionList = versionList)
                }
            }
        }
}

data class CreateInstanceState(
    val instanceName: String = "",
    val gameVersion: String = "",
    val gameVersionList: Result<GameVersionList>? = null,
    val modLoader: ModLoaderType? = null
) : MVIState


sealed interface CreateInstanceIntent : MVIIntent {
}

sealed interface CreateInstanceAction : MVIAction {
}
