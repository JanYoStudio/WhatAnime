package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.gms.ads.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.state.AlertDialog
import pw.janyo.whatanime.ui.state.DialogShowState
import pw.janyo.whatanime.ui.state.rememberDialogShowState
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.utils.getCalendarFromLong
import vip.mystery0.tools.utils.toDateTimeString
import java.io.File
import java.text.DecimalFormat

class HistoryActivity : BaseComposeActivity<HistoryViewModel>() {
    override val viewModel: HistoryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.refresh()
    }

    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val errorMessage by viewModel.errorMessageData.observeAsState()
        val adsDialogShowState = rememberDialogShowState<Boolean>(null)
        val isRefreshing by viewModel.refreshData.observeValueAsState()
        val historyList by viewModel.historyList.observeAsState()
        WhatAnimeTheme {
            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        title = { Text(text = title.toString()) },
                        navigationIcon = {
                            IconButton(onClick = {
                                finish()
                            }) {
                                Icon(Icons.Filled.ArrowBack, "")
                            }
                        },
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                    )
                },
            ) {
                if (inBlackList) {
                    BuildAdLayout(adsDialogShowState)
                }
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    BuildList(historyList)
                }
            }
            BuildAdDialog(adsDialogShowState)
            errorMessage?.let {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            }
        }
    }

    @Composable
    fun BuildAdLayout(adsDialogShowState: DialogShowState<Boolean>) {
        var adLoadResult by remember { mutableStateOf(true) }
        if (!adLoadResult) {
            return
        }
        //初始化AdMod
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        Row {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp),
                factory = { context ->
                    AdView(context).apply {
                        this.adSize = AdSize.BANNER
                        this.adUnitId = "ca-app-pub-6114262658640635/9315758560"
                        loadAd(adRequest)
                        this.adListener = object : AdListener() {
                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                adLoadResult = false
                            }
                        }
                    }
                }
            )
            IconButton(onClick = {
                adsDialogShowState.show(true)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_why_show_ad),
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    fun BuildAdDialog(adsDialogShowState: DialogShowState<Boolean>) {
        AlertDialog(
            dialogShowState = adsDialogShowState,
            confirmButton = {
                TextButton(onClick = { dismiss() }) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            title = {
                Text(text = stringResource(id = R.string.action_why_ad))
            }, text = {
                Text(text = stringResource(id = R.string.hint_why_ads_appear))
            }
        )
    }

    @ExperimentalMaterialApi
    @Composable
    fun BuildList(list: List<AnimationHistory>?) {
        if (list == null) {
            return
        }
        if (list.isEmpty()) {
            viewModel.errorMessageState(stringResource(id = R.string.hint_no_result))
            return
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(list) { item: AnimationHistory ->
                BuildResultItem(history = item)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun BuildResultItem(history: AnimationHistory) {
        val animation: Animation = history.result.fromJson()
        val animationDocs = if (animation.docs.isNotEmpty()) animation.docs[0] else null
        val similarity = if (animationDocs == null)
            "0%"
        else
            "${DecimalFormat("#.0000").format(animationDocs.similarity * 100)}%"
        Card(
            modifier = Modifier.padding(horizontal = 8.dp),
            border = BorderStroke(
                1.dp,
                colorResource(id = R.color.outlined_stroke_color)
            ),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                DetailActivity.showDetail(
                    this@HistoryActivity,
                    File(history.cachePath), history.originPath, history.title
                )
            }
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                Image(
                    painter = rememberCoilPainter(request = File(history.cachePath)),
                    contentDescription = null,
                    modifier = Modifier
                        .height(90.dp)
                        .width(160.dp)
                )
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    BuildText(text = stringResource(id = R.string.hint_time_history))
                    BuildText(text = stringResource(id = R.string.hint_title_native))
                    BuildText(text = stringResource(id = R.string.hint_title_chinese))
                    BuildText(text = stringResource(id = R.string.hint_episode))
                    BuildText(text = stringResource(id = R.string.hint_similarity))
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                ) {
                    BuildText(history.time.getCalendarFromLong().toDateTimeString())
                    BuildText(animationDocs?.title_native ?: "")
                    BuildText(animationDocs?.title_chinese ?: "")
                    BuildText(animationDocs?.episode ?: "")
                    BuildText(similarity)
                }
            }
        }
    }

    @Composable
    fun BuildText(text: String) {
        Text(
            text = text,
            color = MaterialTheme.colors.onSurface,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}
