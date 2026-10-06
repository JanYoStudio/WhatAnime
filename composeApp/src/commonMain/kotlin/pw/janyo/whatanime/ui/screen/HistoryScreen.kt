package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import pw.janyo.whatanime.ui.components.HistoryEmptyState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.model.ReadOnlyAnimationHistory
import pw.janyo.whatanime.ui.components.SwipeToDeleteContainer
import pw.janyo.whatanime.ui.components.WorkspaceMessage
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.navigation.RouteDetail
import pw.janyo.whatanime.utils.formatDecimal
import pw.janyo.whatanime.utils.relativeTimeParts
import pw.janyo.whatanime.utils.RelativeTimeKind
import pw.janyo.whatanime.viewmodel.HistoryViewModel
import whatanime.composeapp.generated.resources.*
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: HistoryViewModel = koinViewModel()) {
    val navController = LocalNavController.current!!
    val state by vm.historyListState.collectAsState()
    val listState = rememberLazyListState()
    var confirmation by remember { mutableStateOf<HistoryDeleteConfirmation?>(null) }
    val busy = state.deletingIds.isNotEmpty()
    BackHandler(state.selectionMode && confirmation == null) { vm.exitSelection() }
    DisposableEffect(vm) { onDispose { vm.exitSelection() } }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(state.errorMessage, state.cleanupFailed, state.failedDeleteCount, busy) {
        if (busy) return@LaunchedEffect
        val message = listOfNotNull(
            if (state.failedDeleteCount > 0) getString(Res.string.ui_batch_delete_failed, state.failedDeleteCount)
            else state.errorMessage.takeIf { it.isNotBlank() },
            if (state.cleanupFailed) getString(Res.string.ui_cleanup_failed) else null,
        ).joinToString("\n")
        if (message.isNotBlank()) {
            snackbar.showSnackbar(message)
            vm.acknowledgeError()
        }
    }
    Scaffold(
        topBar = {
            if (state.selectionMode) {
                // 全选操作独立成行并允许换行，确保窄屏大字下 X、计数及删除仍可见。
                Column {
                    TopAppBar(
                        title = { Text(stringResource(Res.string.ui_selected_count, state.selectedIds.size)) },
                        navigationIcon = { IconButton(onClick = vm::exitSelection) {
                            Icon(Icons.Outlined.Close, stringResource(Res.string.ui_exit_selection))
                        } },
                        actions = { IconButton(enabled = state.selectedIds.isNotEmpty() && !busy, onClick = {
                            confirmation = HistoryDeleteConfirmation(state.selectedIds.toSet(), batch = true)
                        }) { Icon(Icons.Outlined.Delete, stringResource(Res.string.ui_delete_selected)) } },
                    )
                    FlowRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = vm::selectAll, enabled = !busy && state.selectedIds.size < state.list.size) {
                            Text(stringResource(Res.string.ui_select_all))
                        }
                        TextButton(onClick = vm::clearSelection, enabled = !busy && state.selectedIds.isNotEmpty()) {
                            Text(stringResource(Res.string.ui_deselect_all))
                        }
                    }
                }
            } else {
                TopAppBar(title = { Text(stringResource(Res.string.title_activity_history)) }, actions = {
                    IconButton(onClick = vm::refresh, enabled = !state.loading && !busy) {
                        Icon(Icons.Outlined.Refresh, stringResource(Res.string.ui_refresh_history))
                    }
                })
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        PullToRefreshBox(state.loading, { if (!state.selectionMode && !busy) vm.refresh() }, Modifier.padding(padding).fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val emptyMinHeight = (maxHeight - 32.dp).coerceAtLeast(0.dp)
                LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.list.isEmpty()) item {
                        when {
                            state.loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            state.errorMessage.isNotBlank() -> WorkspaceMessage(state.errorMessage, stringResource(Res.string.ui_retry), vm::refresh)
                            state.loaded -> Box(Modifier.fillMaxWidth().heightIn(min = emptyMinHeight), contentAlignment = Alignment.Center) { HistoryEmptyState() }
                        }
                    }
                    items(state.list, key = { it.id }) { item ->
                        SwipeToDeleteContainer(item = item, enabled = !state.selectionMode && !state.loading && confirmation == null && !busy, onDelete = {
                            if (confirmation == null && !busy) confirmation = HistoryDeleteConfirmation(setOf(it.id), title = it.title)
                        }) {
                            HistoryCard(
                                item, enabled = item.id !in state.deletingIds,
                                selectionMode = state.selectionMode, selected = item.id in state.selectedIds,
                                onLongClick = { if (state.selectionMode) vm.toggleSelection(item.id) else vm.beginSelection(item.id) },
                                onClick = {
                                    if (state.selectionMode) vm.toggleSelection(item.id)
                                    else if (item.isOldData) scope.launch { showToast(getString(Res.string.hint_data_convert_no_detail_in_history)) }
                                    else navController.navigate(RouteDetail(item.id, item.cachePath)) { launchSingleTop = true }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    confirmation?.let { request ->
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = { Text(if (request.batch) stringResource(Res.string.ui_delete_selected_title, request.ids.size)
                else stringResource(Res.string.hint_delete, request.title)) },
            text = { Text(stringResource(Res.string.hint_delete_desc)) },
            confirmButton = { TextButton(onClick = {
                if (request.batch) vm.deleteSelected(request.ids) else vm.deleteHistory(request.ids.single())
                confirmation = null
            }) { Text(stringResource(Res.string.action_ok)) } },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
}

private data class HistoryDeleteConfirmation(val ids: Set<Int>, val title: String = "", val batch: Boolean = false)

@Composable
private fun HistoryCard(
    history: ReadOnlyAnimationHistory, enabled: Boolean,
    selectionMode: Boolean, selected: Boolean, onLongClick: () -> Unit, onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            enabled = enabled, role = if (selectionMode) Role.Checkbox else Role.Button,
            onLongClickLabel = stringResource(Res.string.ui_select_history),
            onLongClick = onLongClick, onClick = onClick,
        ).semantics { if (selectionMode) toggleableState = ToggleableState(selected) },
        colors = CardDefaults.elevatedCardColors(containerColor = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        if (!enabled) LinearProgressIndicator(Modifier.fillMaxWidth())
        BoxWithConstraints(Modifier.padding(12.dp)) {
            val vertical = maxWidth < 336.dp || (LocalDensity.current.fontScale >= 1.5f && maxWidth < 376.dp)
            val image: @Composable (Modifier) -> Unit = { modifier ->
                Box(modifier.aspectRatio(16f / 9f)) {
                    AsyncImage(
                        PlatformFile(history.cachePath), null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                        error = painterResource(Res.drawable.ic_load_failed),
                    )
                    if (selectionMode) Surface(
                        Modifier.align(Alignment.TopStart).padding(4.dp),
                        shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface,
                    ) { Checkbox(checked = selected, onCheckedChange = null) }
                }
            }
            val info: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(history.title, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text(relativeTime(history.time), style = MaterialTheme.typography.bodySmall)
                    if (!history.isOldData) Text(
                        stringResource(Res.string.ui_similarity, "${formatDecimal(history.similarity * 100, 1)}%"),
                        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (vertical) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { image(Modifier.fillMaxWidth()); info(Modifier.fillMaxWidth()) }
            else Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) { image(Modifier.width(128.dp)); info(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun relativeTime(time: Long): String {
    val parts = relativeTimeParts(time, Clock.System.now().toEpochMilliseconds())
    return when (parts.kind) {
        RelativeTimeKind.JustNow -> stringResource(Res.string.ui_just_now)
        RelativeTimeKind.Minutes -> stringResource(Res.string.ui_minutes_ago, parts.count)
        RelativeTimeKind.Hours -> stringResource(Res.string.ui_hours_ago, parts.count)
        RelativeTimeKind.Days -> stringResource(Res.string.ui_days_ago, parts.count)
    }
}
