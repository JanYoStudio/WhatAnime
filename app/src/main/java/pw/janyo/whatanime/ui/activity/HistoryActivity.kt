package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.twotone.DeleteSweep
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.tools.utils.getCalendarFromLong
import vip.mystery0.tools.utils.toDateTimeString
import java.io.File
import java.text.DecimalFormat
import kotlin.math.roundToInt

class HistoryActivity : BaseComposeActivity<HistoryViewModel>() {
    override val viewModel: HistoryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observerErrorMessage {
            it.toastLong()
        }
        viewModel.refresh()
    }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val isRefreshing by viewModel.refreshData.observeValueAsState()
        val historyList by viewModel.historyList.observeAsState()
        val selectedList = remember { mutableStateListOf<Int>() }
        val selectedMode by remember { derivedStateOf { selectedList.isNotEmpty() } }
        WhatAnimeTheme {
            val scaffoldState = rememberScaffoldState()
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        title = { Text(text = title.toString()) },
                        navigationIcon = {
                            IconButton(onClick = {
                                finish()
                            }) {
                                Icon(
                                    Icons.Filled.ArrowBack,
                                    contentDescription = "",
                                    tint = MaterialTheme.colors.onPrimary
                                )
                            }
                        },
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
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
                            Icon(
                                imageVector = Icons.TwoTone.DeleteSweep,
                                contentDescription = null
                            )
                        }
                    }
                },
            ) { innerPadding ->
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(innerPadding)
                ) {
                    BuildList(historyList, selectedList)
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun BuildList(
        list: List<AnimationHistory>?,
        selectedList: SnapshotStateList<Int>
    ) {
        if (list == null) {
            return
        }
        if (list.isEmpty()) {
            viewModel.errorMessageState(stringResource(R.string.hint_no_result))
            return
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(list) { item: AnimationHistory ->
                BuildResultItem(history = item, selectedList = selectedList)
            }
        }
    }

    @ExperimentalMaterialApi
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
                MaterialTheme.colors.secondary
            )
        else
            BorderStroke(
                1.dp,
                colorResource(R.color.outlined_stroke_color)
            )
        val swipeableState = rememberSwipeableState(0,
            confirmStateChange = {
                if (it != 0) {
                    reverseState()
                }
                false
            })
        val sizePx = with(LocalDensity.current) { 48.dp.toPx() }
        val anchors = mapOf(-sizePx to -1, 0f to 0, sizePx to 1)

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { reverseState() },
                        onTap = {
                            debugOnClick("缓存图片路径 ${history.cachePath}") {
                                when {
                                    selectedList.isNotEmpty() -> reverseState()
                                    isOldData -> getString(R.string.hint_data_convert_no_detail_in_history).toastLong()
                                    else -> DetailActivity.showDetail(
                                        this@HistoryActivity,
                                        File(history.cachePath), history.originPath, history.title
                                    )
                                }
                            }
                        },
                    )
                }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                ),
            border = cardBorder,
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
            ) {
                Image(
                    painter = rememberImagePainter(data = File(history.cachePath)),
                    contentDescription = null,
                    modifier = Modifier
                        .height(90.dp)
                        .width(160.dp)
                )
                val isValidEpisode = history.episode != ""
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    BuildText(stringResource(R.string.hint_time_history))
                    BuildText(stringResource(R.string.hint_title_native), FontWeight.Bold)
                    if (!isOldData) {
                        BuildText(stringResource(R.string.hint_ani_list_id))
                    }
                    if (!isOldData && isValidEpisode) {
                        BuildText(stringResource(R.string.hint_episode))
                    }
                    if (!isOldData) {
                        BuildText(stringResource(R.string.hint_similarity), FontWeight.Bold)
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
                    if (!isOldData && isValidEpisode) {
                        BuildText(history.episode)
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
            color = MaterialTheme.colors.onSurface,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = fontWeight
        )
    }
}
