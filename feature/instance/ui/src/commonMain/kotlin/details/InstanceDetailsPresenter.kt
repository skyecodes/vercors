package app.vercors.feature.instance.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import app.vercors.core.ui.state.collectProgressiveAsState
import app.vercors.feature.config.domain.usecase.ObserveConfigurationUseCase
import app.vercors.feature.instance.domain.model.ContentKind
import app.vercors.feature.instance.domain.usecase.ObserveInstanceUseCase
import app.vercors.feature.instance.ui.details.content.InstalledContentPresenter
import app.vercors.feature.instance.ui.details.header.InstanceHeaderPresenter
import app.vercors.feature.instance.ui.details.installation.InstanceInstallationPresenter
import app.vercors.feature.instance.ui.details.launch.InstanceLaunchPresenter
import app.vercors.feature.instance.ui.details.logs.InstanceLogsPresenter
import app.vercors.feature.instance.ui.details.options.InstanceOptionsPresenter
import app.vercors.feature.instance.ui.details.servers.InstanceServersPresenter
import app.vercors.feature.instance.ui.details.update.InstanceUpdatePresenter
import app.vercors.feature.instance.ui.details.worlds.InstalledWorldsPresenter
import app.vercors.feature.project.domain.model.from
import app.vercors.feature.project.ui.details.ProjectDetailsScreen
import app.vercors.feature.project.ui.list.ProjectListScreen
import app.vercors.shared.ui.Presenter
import app.vercors.shared.ui.Store
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import opensavvy.state.progressive.valueOrNull

@AssistedInject
class InstanceDetailsPresenter(
    @Assisted private val screen: InstanceDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val observeInstance: ObserveInstanceUseCase,
    private val observeConfiguration: ObserveConfigurationUseCase,
    private val instanceLaunchPresenterFactory: InstanceLaunchPresenter.Factory,
    private val instanceHeaderPresenterFactory: InstanceHeaderPresenter.Factory,
    private val instanceInstallationPresenterFactory: InstanceInstallationPresenter.Factory,
    private val instanceUpdatePresenterFactory: InstanceUpdatePresenter.Factory,
    private val instanceOptionsPresenterFactory: InstanceOptionsPresenter.Factory,
    private val installedContentPresenterFactory: InstalledContentPresenter.Factory,
    private val installedWorldsPresenterFactory: InstalledWorldsPresenter.Factory,
    private val instanceServersPresenterFactory: InstanceServersPresenter.Factory,
    private val instanceLogsPresenterFactory: InstanceLogsPresenter.Factory,
) : Presenter<InstanceDetailsState, InstanceDetailsEvent> {
    @Composable
    override fun present(): Store<InstanceDetailsState, InstanceDetailsEvent> {
        val instanceId = screen.instanceId
        val instance by observeInstanceView(observeInstance, instanceId)
        val configuration by observeConfiguration().collectProgressiveAsState()
        val (selectedTab, setSelectedTab) = rememberSaveable {
            mutableStateOf(screen.initialTab ?: InstanceDetailsTab.Options)
        }

        val launch = remember(instanceId) {
            instanceLaunchPresenterFactory.create(instanceId, screen.autoLaunch) {
                setSelectedTab(InstanceDetailsTab.Logs)
            }
        }.present()
        val header = remember(instanceId) { instanceHeaderPresenterFactory.create(instanceId, navigator) }.present()
        val installation = remember(instanceId) { instanceInstallationPresenterFactory.create(instanceId) }.present()
        val update = remember(instanceId) { instanceUpdatePresenterFactory.create(instanceId) }.present()
        val optionsContent = remember(instanceId) { instanceOptionsPresenterFactory.create(instanceId) }.present()
        val modsContent = remember(instanceId) {
            installedContentPresenterFactory.create(instanceId, ContentKind.Mod)
        }.present()
        val resourcePacksContent = remember(instanceId) {
            installedContentPresenterFactory.create(instanceId, ContentKind.ResourcePack)
        }.present()
        val shaderPacksContent = remember(instanceId) {
            installedContentPresenterFactory.create(instanceId, ContentKind.ShaderPack)
        }.present()
        val worldsContent = remember(instanceId) { installedWorldsPresenterFactory.create(instanceId) }.present()
        val serversContent = remember(instanceId) { instanceServersPresenterFactory.create(instanceId) }.present()
        val logsContent = remember(instanceId) { instanceLogsPresenterFactory.create(instanceId) }.present()

        val currentInstance = instance.valueOrNull
        return InstanceDetailsState(
            instance = instance,
            selectedTab = selectedTab,
            horizontalTabs = configuration.valueOrNull?.horizontalTabs == true,
            launch = launch,
            header = header,
            installation = installation,
            update = update,
            optionsContent = optionsContent,
            modsContent = modsContent,
            resourcePacksContent = resourcePacksContent,
            shaderPacksContent = shaderPacksContent,
            worldsContent = worldsContent,
            serversContent = serversContent,
            logsContent = logsContent,
        ) + { event ->
            handleEvent(
                event = event,
                currentInstance = currentInstance,
                setSelectedTab = setSelectedTab,
            )
        }
    }

    private fun handleEvent(
        event: InstanceDetailsEvent,
        currentInstance: InstanceView?,
        setSelectedTab: (InstanceDetailsTab) -> Unit,
    ) {
        when (event) {
            is InstanceDetailsEvent.OnTabClick -> setSelectedTab(event.tab)
            is InstanceDetailsEvent.OnOpenProjectClick -> navigator.goTo(
                ProjectDetailsScreen(event.projectId from event.platformId, preferredInstanceId = screen.instanceId)
            )

            is InstanceDetailsEvent.OnAddContentClick -> navigator.goTo(
                ProjectListScreen(
                    initialType = event.type,
                    initialGameVersion = currentInstance?.gameVersion,
                    initialLoader = currentInstance?.loaderType,
                    sourceInstanceId = screen.instanceId
                )
            )
        }
    }

    @CircuitInject(InstanceDetailsScreen::class, AppScope::class)
    @AssistedFactory
    fun interface Factory {
        fun create(screen: InstanceDetailsScreen, navigator: Navigator): InstanceDetailsPresenter
    }
}
