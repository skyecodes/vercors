package app.vercors.feature.instance.ui.details

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercors.core.ui.component.AppIcon
import app.vercors.core.ui.component.AppIconTextButton
import app.vercors.core.ui.component.AppText
import app.vercors.core.ui.layout.AppLoading
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.onSuccess
import app.vercors.core.ui.theme.success
import app.vercors.core.ui.theme.surface2
import app.vercors.feature.instance.domain.model.InstanceStatus
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.noServersAdded
import app.vercors.feature.instance.ui.quickJoin
import app.vercors.feature.launcher.domain.model.QuickPlayTarget
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.Store
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Server
import com.composeunstyled.theme.Theme
import org.jetbrains.compose.resources.stringResource

@Composable
context(eventSink: EventSink<InstanceDetailsEvent>)
fun InstanceServersTab(
    instance: InstanceView,
    content: Store<InstanceServersState, InstanceServersEvent>,
    horizontalTabs: Boolean
) {
    val canQuickJoin = instance.status is InstanceStatus.Idle || instance.status is InstanceStatus.Failed
    InstalledServerList(
        servers = content.state.servers,
        canQuickJoin = canQuickJoin,
        onQuickJoinClick = { address ->
            eventSink(InstanceDetailsEvent.QuickJoin(QuickPlayTarget.Multiplayer(address)))
        },
        horizontalTabs = horizontalTabs
    )
}

@Composable
private fun InstalledServerList(
    servers: List<InstalledServerView>?,
    canQuickJoin: Boolean,
    onQuickJoinClick: (String) -> Unit,
    horizontalTabs: Boolean,
) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .let { if (!horizontalTabs) it.padding(vertical = 8.dp) else it }
            .appCard()) {
        when {
            servers == null -> AppLoading()
            servers.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppText(stringResource(InstanceRes.string.noServersAdded))
            }

            else -> Row(modifier = Modifier.fillMaxSize().padding(end = 4.dp)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(servers, key = { it.address + it.name }) { server ->
                        InstalledServerRow(server, canQuickJoin, onQuickJoinClick)
                    }
                }
                VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
            }
        }
    }
}

@Composable
private fun InstalledServerRow(server: InstalledServerView, canQuickJoin: Boolean, onQuickJoinClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(Theme[colors][surface2]),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(imageVector = Lucide.Server, contentDescription = null, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(
                text = server.name,
                fontWeight = FontWeight.SemiBold,
                singleLine = true,
                overflow = TextOverflow.Ellipsis
            )
            AppText(text = server.address, fontSize = 12.sp, singleLine = true, overflow = TextOverflow.Ellipsis)
        }

        AppIconTextButton(
            onClick = { onQuickJoinClick(server.address) },
            text = stringResource(InstanceRes.string.quickJoin),
            imageVector = Lucide.Play,
            enabled = canQuickJoin,
            backgroundColor = Theme[colors][success],
            contentColor = Theme[colors][onSuccess]
        )
    }
}
