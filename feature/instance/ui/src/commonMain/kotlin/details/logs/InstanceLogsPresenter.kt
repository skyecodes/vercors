package app.vercors.feature.instance.ui.details.logs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.domain.toggle
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.config.domain.usecase.UpdateConfigurationUseCase
import app.vercors.feature.instance.domain.model.InstanceId
import app.vercors.feature.instance.domain.usecase.ObserveInstanceLogsUseCase
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.retained.produceAndCollectAsRetainedState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import opensavvy.state.progressive.valueOrNull

@AssistedInject
class InstanceLogsPresenter(
    @Assisted private val instanceId: InstanceId,
    private val observeInstanceLogs: ObserveInstanceLogsUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val updateConfiguration: UpdateConfigurationUseCase,
    private val globalScope: CoroutineScope,
) : Presenter<InstanceLogsState, InstanceLogsEvent> {
    @Composable
    override fun present(): Store<InstanceLogsState, InstanceLogsEvent> {
        val logs by produceAndCollectAsRetainedState(instanceId, initial = null) {
            observeInstanceLogs(instanceId).map { list -> list?.map { it.toView() } }
        }
        val (searchQuery, setSearchQuery) = rememberSaveable { mutableStateOf("") }
        val (levelFilter, setLevelFilter) = rememberSaveable {
            mutableStateOf(InstanceLogEventView.Level.entries.toSet())
        }
        val configuration by observeConfiguration().collectProgressiveAsState()

        return InstanceLogsState(
            logs = logs,
            searchQuery = searchQuery,
            levelFilter = levelFilter,
            wrapLines = configuration.valueOrNull?.wrapLogLines ?: true,
        ) + { event ->
            handleEvent(
                event = event,
                levelFilter = levelFilter,
                setSearchQuery = setSearchQuery,
                setLevelFilter = setLevelFilter,
            )
        }
    }

    private fun handleEvent(
        event: InstanceLogsEvent,
        levelFilter: Set<InstanceLogEventView.Level>,
        setSearchQuery: (String) -> Unit,
        setLevelFilter: (Set<InstanceLogEventView.Level>) -> Unit,
    ) {
        when (event) {
            is InstanceLogsEvent.OnSearchQueryChange -> setSearchQuery(event.query)
            is InstanceLogsEvent.OnLevelFilterToggle -> setLevelFilter(levelFilter.toggle(event.level).toSet())
            InstanceLogsEvent.OnWrapLinesToggle -> globalScope.launch {
                updateConfiguration { copy(wrapLogLines = !wrapLogLines) }
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(instanceId: InstanceId): InstanceLogsPresenter
    }
}
