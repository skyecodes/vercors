package app.vercors.core.ui.layout

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.modifier.appCard
import com.composeunstyled.Tab
import com.composeunstyled.TabList
import com.composeunstyled.TabPanel
import com.composeunstyled.UnstyledTabGroup

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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabList(
                    modifier = Modifier.fillMaxWidth(),
                    orientation = orientation
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).appCard().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        tabs.forEach { tab ->
                            Tab(
                                key = tab,
                                modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(4.dp)),
                                indication = LocalIndication.current,
                            ) {
                                AppTabScope(selected, enabled).tabTitle(tab)
                            }
                        }
                    }
                }

                tabs.forEach { tab ->
                    TabPanel(
                        key = tab,
                        modifier = Modifier.fillMaxSize().appCard()
                    ) {
                        tabContent(tab)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabList(
                    modifier = Modifier.fillMaxHeight(),
                    orientation = orientation
                ) {
                    Column(
                        modifier = Modifier.width(IntrinsicSize.Max).appCard().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        tabs.forEach { tab ->
                            Tab(
                                key = tab,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                                indication = LocalIndication.current,
                            ) {
                                AppTabScope(selected, enabled).tabTitle(tab)
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
)
