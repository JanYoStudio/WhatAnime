package pw.janyo.whatanime.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.getCalendarFromLong
import pw.janyo.whatanime.utils.toDateTimeString
import pw.janyo.whatanime.viewModel.HistoryViewModel
import java.io.File
import java.text.DecimalFormat
import kotlin.math.roundToInt

class HistoryActivity : BaseComposeActivity() {
    private val viewModel: HistoryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val listState by viewModel.historyListState.collectAsState()

        val selectedList = remember { mutableStateListOf<Int>() }
        val selectedMode by remember { derivedStateOf { selectedList.isNotEmpty() } }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            Icons(Icons.AutoMirrored.Filled.ArrowBack)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            R.string.hint_swipe_to_delete.toast()
                        }) {
                            Icons(Icons.Outlined.TipsAndUpdates)
                        }
                    }
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = selectedMode,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    FloatingActionButton(onClick = {
                        viewModel.deleteHistory(selectedList)
                    }) {
                        Icons(Icons.Outlined.DeleteSweep)
                    }
                }
            },
        ) { innerPadding ->
            val pullRefreshState = rememberPullRefreshState(
                refreshing = listState.loading,
                onRefresh = {
                    viewModel.refresh()
                },
            )
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .pullRefresh(pullRefreshState)
            ) {
                BuildList(Modifier.fillMaxSize(), listState.list, selectedList)
                PullRefreshIndicator(
                    refreshing = listState.loading,
                    state = pullRefreshState,
                    Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }

    @Composable
    fun BuildList(
        modifier: Modifier,
        list: List<AnimationHistory>,
        selectedList: SnapshotStateList<Int>
    ) {
        if (list.isEmpty()) {
            BuildNoDataLayout(modifier)
        } else {
            LazyColumn(
                modifier = modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list) { item: AnimationHistory ->
                    BuildResultItem(
                        history = item,
                        selectedList = selectedList,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BuildResultItem(
        history: AnimationHistory,
        selectedList: SnapshotStateList<Int>,
    ) {
        val similarity = "${DecimalFormat("#.0000").format(history.similarity * 100)}%"
        val isOldData =
            history.episode == "old" || history.similarity == 0.0

        fun reverseState() {
            if (selectedList.contains(history.id)) {
                selectedList.remove(history.id)
            } else {
                selectedList.add(history.id)
            }
        }

        val cardBorder = if (selectedList.contains(history.id))
            BorderStroke(
                4.dp,
                MaterialTheme.colorScheme.primary
            )
        else
            null
        val density = LocalDensity.current
        val anchors = with(density) {
            DraggableAnchors {
                DragState.Start at -48.dp.toPx()
                DragState.None at 0F
                DragState.End at 48.dp.toPx()
            }
        }
        val lastState = remember { mutableStateOf(DragState.None) }
        val state = remember {
            AnchoredDraggableState(
                initialValue = DragState.None,
                anchors = anchors,
                animationSpec = spring(),
                confirmValueChange = {
                    if (it != DragState.None && it != lastState.value) {
                        reverseState()
                    }
                    lastState.value = it
                    false
                },
                positionalThreshold = { it * 0.3F },
                velocityThreshold = { with(density) { 125.dp.toPx() } },
            )
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { reverseState() },
                        onTap = {
                            when {
                                selectedList.isNotEmpty() ->
                                    reverseState()

                                isOldData ->
                                    R.string.hint_data_convert_no_detail_in_history.toast(true)

                                else ->
                                    intentTo(
                                        DetailActivity::class,
                                        DetailActivity.showDetail(history)
                                    )
                            }
                        },
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                ),
            border = cardBorder,
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .offset {
                        IntOffset(
                            state
                                .requireOffset()
                                .roundToInt(), 0
                        )
                    }
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(history.cachePath))
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .height(90.dp)
                        .width(160.dp),
                )
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    BuildText(stringResource(R.string.history_hint_save_time))
                    BuildText(stringResource(R.string.history_hint_native_title), FontWeight.Bold)
                    if (!isOldData) {
                        BuildText(stringResource(R.string.history_hint_ani_list_id))
                    }
                    if (!isOldData) {
                        BuildText(stringResource(R.string.history_hint_similarity), FontWeight.Bold)
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                ) {
                    BuildText(history.time.getCalendarFromLong().toDateTimeString())
                    BuildText(history.title, FontWeight.Bold)
                    if (!isOldData) {
                        BuildText(history.anilistId.toString())
                    }
                    if (!isOldData) {
                        BuildText(similarity, FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    fun BuildText(text: String, fontWeight: FontWeight? = null) {
        Text(
            text = text,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = fontWeight
        )
    }
}

enum class DragState {
    None, Start, End
}
