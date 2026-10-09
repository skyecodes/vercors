package app.vercors.feature.instance.ui.details.logs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.vercors.core.ui.component.*
import app.vercors.core.ui.modifier.appCard
import app.vercors.core.ui.theme.*
import app.vercors.feature.instance.ui.InstanceRes
import app.vercors.feature.instance.ui.wrapLines
import app.vercors.shared.ui.EventSink
import app.vercors.shared.ui.Store
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.theme.Theme
import kotlinx.coroutines.flow.filter
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstanceLogs(content: Store<InstanceLogsState, InstanceLogsEvent>, horizontalTabs: Boolean) {
    val (state, eventSink) = content
    val logs = state.filteredLogs ?: emptyList()
    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    var stickToBottom by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { inProgress -> !inProgress }
            .collect { stickToBottom = !listState.canScrollForward }
    }
    LaunchedEffect(logs) {
        if (stickToBottom && logs.isNotEmpty()) listState.scrollToItem(logs.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .let { if (!horizontalTabs) it.padding(vertical = 8.dp) else it },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        with(eventSink) {
            InstanceLogsFilterBar(state)
        }

        SelectionContainer(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.appCard()) {
                Row(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            .let { m -> if (state.wrapLines) m else m.horizontalScroll(horizontalScrollState) },
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(logs) { logEvent ->
                            ProvideTextStyle(Theme[textStyles][console]) {
                                InstanceLogMessage(logEvent, state.wrapLines)
                            }
                        }
                    }
                    VerticalScrollbar(adapter = rememberScrollbarAdapter(listState))
                }
                if (!state.wrapLines) {
                    HorizontalScrollbar(
                        adapter = rememberScrollbarAdapter(horizontalScrollState),
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
context(eventSink: EventSink<InstanceLogsEvent>)
private fun InstanceLogsFilterBar(state: InstanceLogsState) {
    Row(
        modifier = Modifier.appCard().padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppMultiSelectField(
            values = state.levelFilter,
            items = InstanceLogEventView.Level.entries,
            onValueChange = { eventSink(InstanceLogsEvent.OnLevelFilterToggle(it)) },
            itemRenderer = { value, _ -> AppSelectItem(text = value.name) },
        )
        AppSearchField(
            initialValue = state.searchQuery,
            onValueChange = { eventSink(InstanceLogsEvent.OnSearchQueryChange(it)) },
            modifier = Modifier.weight(1f)
        )
        AppCheckbox(
            label = stringResource(InstanceRes.string.wrapLines),
            checked = state.wrapLines,
            onCheckedChange = { eventSink(InstanceLogsEvent.OnWrapLinesToggle) }
        )
    }
}

private const val MAX_LOG_LINE_LENGTH = 4000

private fun String.truncateLongLines(maxLength: Int = MAX_LOG_LINE_LENGTH): String =
    if (length <= maxLength && '\n' !in this) this
    else lineSequence().joinToString("\n") { if (it.length > maxLength) it.take(maxLength) + "…" else it }

@Composable
private fun InstanceLogMessage(
    it: InstanceLogEventView,
    wrapLines: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (it.message.isEmpty()) AppText(" ") else {
            val timezone = TimeZone.currentSystemDefault()
            val format = remember {
                LocalDateTime.Format {
                    hour()
                    char(':')
                    minute()
                    char(':')
                    second()
                }
            }
            val color = if (it.isLauncher) Theme[colors][primary] else when (it.level) {
                InstanceLogEventView.Level.TRACE, InstanceLogEventView.Level.DEBUG, InstanceLogEventView.Level.INFO -> Theme[colors][success]
                InstanceLogEventView.Level.WARN -> Theme[colors][warn]
                InstanceLogEventView.Level.ERROR, InstanceLogEventView.Level.FATAL -> Theme[colors][error]
            }
            AppText(
                text = remember(it.timestamp, timezone, format) {
                    it.timestamp.toLocalDateTime(timezone).format(format)
                },
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center,
                color = Theme[colors][onBackground].copy(alpha = 0.5f)
            )
            Box(
                modifier = Modifier.background(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ).padding(horizontal = 4.dp).width(40.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = it.level.name,
                    color = color
                )
            }
            AppText(
                text = remember(it.message) { it.message.truncateLongLines() },
                color = color,
                maxLines = if (wrapLines) Int.MAX_VALUE else 1,
            )
        }
    }
}
