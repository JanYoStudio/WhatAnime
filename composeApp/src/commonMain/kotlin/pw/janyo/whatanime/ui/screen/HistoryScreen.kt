package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.clickable
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
    var confirmation by remember { mutableStateOf<ReadOnlyAnimationHistory?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(state.errorMessage, state.cleanupFailed) {
        val message = if (state.cleanupFailed) getString(Res.string.ui_cleanup_failed) else state.errorMessage
        if (message.isNotBlank()) {
            snackbar.showSnackbar(message)
            vm.acknowledgeError()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(Res.string.title_activity_history)) }, actions = {
                IconButton(onClick = vm::refresh, enabled = !state.loading) { Icon(Icons.Outlined.Refresh, stringResource(Res.string.ui_refresh_history)) }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        PullToRefreshBox(state.loading, vm::refresh, Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                state = listState, modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.list.isEmpty()) item {
                    when {
                        state.loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        state.errorMessage.isNotBlank() -> WorkspaceMessage(state.errorMessage, stringResource(Res.string.ui_retry), vm::refresh)
                        state.loaded -> WorkspaceMessage(stringResource(Res.string.ui_no_history))
                    }
                }
                items(state.list, key = { it.id }) { item ->
                    SwipeToDeleteContainer(item = item, enabled = confirmation == null && item.id !in state.deletingIds, onDelete = {
                        if (confirmation == null && it.id !in state.deletingIds) confirmation = it
                    }) {
                        HistoryCard(item, enabled = item.id !in state.deletingIds) {
                            if (item.id in state.deletingIds) return@HistoryCard
                            if (item.isOldData) scope.launch { showToast(getString(Res.string.hint_data_convert_no_detail_in_history)) }
                            else navController.navigate(RouteDetail(item.id, item.cachePath)) { launchSingleTop = true }
                        }
                    }
                }
            }
        }
    }
    confirmation?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmation = null },
            title = { Text(stringResource(Res.string.hint_delete, item.title)) }, text = { Text(stringResource(Res.string.hint_delete_desc)) },
            confirmButton = { TextButton(onClick = { vm.deleteHistory(item.id); confirmation = null }) { Text(stringResource(Res.string.action_ok)) } },
            dismissButton = { TextButton(onClick = { confirmation = null }) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
}

@Composable
private fun HistoryCard(history: ReadOnlyAnimationHistory, enabled: Boolean, onClick: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)) {
        if (!enabled) LinearProgressIndicator(Modifier.fillMaxWidth())
        BoxWithConstraints(Modifier.padding(12.dp)) {
            val vertical = maxWidth < 336.dp || (LocalDensity.current.fontScale >= 1.5f && maxWidth < 376.dp)
            val image: @Composable (Modifier) -> Unit = { modifier ->
                AsyncImage(
                    PlatformFile(history.cachePath), null,
                    modifier = modifier.aspectRatio(16f / 9f), contentScale = ContentScale.Crop,
                    error = painterResource(Res.drawable.ic_load_failed),
                )
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
