package app.vercors.core.ui.layout

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.animation.appAnimateColorAsState
import app.vercors.core.ui.component.AppIconTextButtonContent
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.modifier.horizontalMouseWheelScroll
import app.vercors.core.ui.theme.colors
import app.vercors.core.ui.theme.surface
import app.vercors.core.ui.theme.surface2
import com.composeunstyled.Tab
import com.composeunstyled.TabList
import com.composeunstyled.TabPanel
import com.composeunstyled.UnstyledTabGroup
import com.composeunstyled.theme.Theme

@Composable
fun <T> AppTabGroup(
    selectedTab: T,
    onSelectedTabChange: (T) -> Unit,
    tabs: List<T>,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    tabTitle: @Composable AppTabScope.(T) -> Unit,
    tabContent: @Composable (T) -> Unit
) {
    UnstyledTabGroup(
        selectedTab = selectedTab,
        onSelectedTabChange = onSelectedTabChange,
        tabs = tabs,
        modifier = modifier
    ) {
        if (orientation == Orientation.Horizontal) {
            Column(
                modifier = Modifier.padding(8.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabList(
                    modifier = Modifier.fillMaxWidth(),
                    orientation = orientation
                ) {
                    val tabListScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).appCard().padding(8.dp)
                            .horizontalScroll(tabListScrollState)
                            .horizontalMouseWheelScroll(tabListScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tabs.forEach { tab ->
                            Tab(
                                key = tab,
                                modifier = Modifier.fillMaxHeight().clip(RoundedCornerShape(4.dp)),
                                indication = LocalIndication.current,
                            ) {
                                AppTabScope(selected, enabled, orientation).tabTitle(tab)
                            }
                        }
                    }
                }

                tabs.forEach { tab ->
                    TabPanel(
                        key = tab,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        tabContent(tab)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabList(
                    modifier = Modifier.fillMaxHeight(),
                    orientation = orientation
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp).width(IntrinsicSize.Max).appCard().padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        tabs.forEach { tab ->
                            Tab(
                                key = tab,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                indication = LocalIndication.current,
                            ) {
                                AppTabScope(selected, enabled, orientation).tabTitle(tab)
                            }
                        }
                    }
                }

                tabs.forEach { tab ->
                    TabPanel(
                        key = tab,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        tabContent(tab)
                    }
                }
            }
        }
    }
}

class AppTabScope internal constructor(
    val selected: Boolean,
    val enabled: Boolean,
    val orientation: Orientation,
)

@Composable
fun AppTabScope.AppSidebarTabItem(title: String, icon: ImageVector) {
    val backgroundColor by appAnimateColorAsState(if (selected) Theme[colors][surface2] else Theme[colors][surface])
    Row(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand, true)
            .padding(8.dp),
        horizontalArrangement = if (orientation == Orientation.Horizontal) {
            Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        } else {
            Arrangement.spacedBy(8.dp)
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconTextButtonContent(text = title, imageVector = icon)
    }
}
