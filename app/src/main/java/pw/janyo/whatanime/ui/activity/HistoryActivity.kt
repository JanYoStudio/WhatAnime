package pw.janyo.whatanime.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.components.SwipeToDeleteContainer
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.getCalendarFromLong
import pw.janyo.whatanime.utils.toDateTimeString
import pw.janyo.whatanime.viewModel.HistoryViewModel
import java.io.File
import java.text.DecimalFormat

class HistoryActivity : BaseComposeActivity() {
    private val viewModel: HistoryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val listState by viewModel.historyListState.collectAsState()

        val animeDialogState = remember { mutableStateOf<AnimationHistory?>(null) }

        LaunchedEffect(Unit) {
            viewModel.refresh()
        }

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
        ) { innerPadding ->
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                modifier = Modifier.padding(innerPadding),
                isRefreshing = listState.loading,
                onRefresh = {
                    viewModel.refresh()
                    animeDialogState.value = null
                },
                state = pullToRefreshState,
            ) {
                BuildList(Modifier.fillMaxSize(), listState.list) {
                    animeDialogState.value = it
                }
            }
            BuildAlertDialog(animeDialogState)
        }
    }

    @Composable
    fun BuildAlertDialog(animeDialogState: MutableState<AnimationHistory?>) {
        if (animeDialogState.value == null) return
        val item = animeDialogState.value!!
        AlertDialog(
            onDismissRequest = { animeDialogState.value = null },
            title = {
                Text(
                    text = stringResource(R.string.hint_delete, item.title)
                )
            },
            text = {
                Text(text = stringResource(R.string.hint_delete_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHistory(item.id)
                        animeDialogState.value = null
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { animeDialogState.value = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    @Composable
    fun BuildList(
        modifier: Modifier,
        list: List<AnimationHistory>,
        onDelete: (AnimationHistory) -> Unit,
    ) {
        if (list.isEmpty()) {
            BuildNoDataLayout(modifier)
        } else {
            LazyColumn(
                modifier = modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list) { item: AnimationHistory ->
                    SwipeToDeleteContainer(
                        item = item,
                        onDelete = onDelete,
                    ) {
                        BuildResultItem(
                            history = item,
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BuildResultItem(
        history: AnimationHistory,
    ) {
        val similarity = "${DecimalFormat("#.0000").format(history.similarity * 100)}%"
        val isOldData =
            history.episode == "old" || history.similarity == 0.0

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .clickable {
                    when {
                        isOldData ->
                            R.string.hint_data_convert_no_detail_in_history.toast(true)

                        else ->
                            intentTo(
                                DetailActivity::class,
                                DetailActivity.showDetail(history)
                            )
                    }
                },
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
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
