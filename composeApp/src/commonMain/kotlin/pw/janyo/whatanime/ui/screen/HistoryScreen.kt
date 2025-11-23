package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.model.ReadOnlyAnimationHistory
import pw.janyo.whatanime.ui.components.NoDataLayout
import pw.janyo.whatanime.ui.components.SwipeToDeleteContainer
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.navigation.RouteDetail
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.formatDateTime
import pw.janyo.whatanime.utils.formatDecimal
import pw.janyo.whatanime.viewmodel.HistoryViewModel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.action_cancel
import whatanime.composeapp.generated.resources.action_ok
import whatanime.composeapp.generated.resources.hint_data_convert_no_detail_in_history
import whatanime.composeapp.generated.resources.hint_delete
import whatanime.composeapp.generated.resources.hint_delete_desc
import whatanime.composeapp.generated.resources.hint_swipe_to_delete
import whatanime.composeapp.generated.resources.history_hint_ani_list_id
import whatanime.composeapp.generated.resources.history_hint_native_title
import whatanime.composeapp.generated.resources.history_hint_save_time
import whatanime.composeapp.generated.resources.history_hint_similarity
import whatanime.composeapp.generated.resources.title_activity_history
import kotlin.time.Instant

@Composable
fun HistoryScreen() {
    val navController = LocalNavController.current!!
    val vm = koinViewModel<HistoryViewModel>()

    val listState by vm.historyListState.collectAsState()

    val animeDialogState = remember { mutableStateOf<ReadOnlyAnimationHistory?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(Res.string.title_activity_history)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icons(Icons.AutoMirrored.Filled.ArrowBack)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            showToast(getString(Res.string.hint_swipe_to_delete))
                        }
                    }) {
                        Icons(Icons.Outlined.TipsAndUpdates)
                    }
                }
            )
        },
    ) { innerPadding ->
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = listState.loading,
            onRefresh = {
                vm.refresh()
                animeDialogState.value = null
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = listState.loading,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            if (listState.list.isEmpty()) {
                NoDataLayout(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(listState.list) { item: ReadOnlyAnimationHistory ->
                        SwipeToDeleteContainer(
                            item = item,
                            onDelete = {
                                animeDialogState.value = it
                            },
                        ) {
                            BuildResultItem(
                                history = item,
                                onClickOldData = {
                                    scope.launch {
                                        showToast(getString(Res.string.hint_data_convert_no_detail_in_history))
                                    }
                                },
                                onClick = {
                                    navController.navigate(
                                        RouteDetail(
                                            item.id,
                                            item.cachePath
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        BuildAlertDialog(animeDialogState, onOk = {
            vm.deleteHistory(it.id)
        })
        LaunchedEffect(listState) {
            if (!listState.loading) {
                pullToRefreshState.animateToHidden()
            }
        }
    }
}

@Composable
private fun BuildAlertDialog(
    animeDialogState: MutableState<ReadOnlyAnimationHistory?>,
    onOk: (ReadOnlyAnimationHistory) -> Unit,
) {
    if (animeDialogState.value == null) return
    val item = animeDialogState.value!!
    AlertDialog(
        onDismissRequest = { animeDialogState.value = null },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.hint_delete, item.title)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(Res.string.hint_delete_desc))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onOk(item)
                    animeDialogState.value = null
                }
            ) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { animeDialogState.value = null }) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BuildResultItem(
    history: ReadOnlyAnimationHistory,
    onClickOldData: () -> Unit,
    onClick: () -> Unit,
) {
    val similarity = "${formatDecimal(history.similarity * 100, 4)}%"
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable {
                if (history.isOldData) {
                    onClickOldData()
                } else {
                    onClick()
                }
            },
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(PlatformFile(history.cachePath))
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .height(90.dp)
                    .width(160.dp),
            )
            val annotationText = buildAnnotatedString {
                append(stringResource(Res.string.history_hint_save_time))
                append(
                    Instant.fromEpochMilliseconds(history.time)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).formatDateTime()
                )
                appendLine()
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(Res.string.history_hint_native_title))
                    append(history.title)
                }
                if (!history.isOldData) {
                    appendLine()
                    append(stringResource(Res.string.history_hint_ani_list_id))
                    append(history.anilistId.toString())
                    appendLine()
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(Res.string.history_hint_similarity))
                        append(similarity)
                    }
                }
            }
            SelectionContainer {
                Text(
                    text = annotationText,
                    fontSize = 12.sp,
                )
            }
        }
    }
}