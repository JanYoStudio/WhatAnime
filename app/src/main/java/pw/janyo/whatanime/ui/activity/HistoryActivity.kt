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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.gms.ads.*
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.constant.Constant.ADMOB_ID
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.viewModel.HistoryViewModel
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
        val adsDialogShowState = remember { mutableStateOf(false) }
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
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Column {
                        if (inBlackList) {
                            BuildAdLayout(adsDialogShowState)
                        }
                        BuildList(historyList)
                    }
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
    fun BuildAdLayout(adsDialogShowState: MutableState<Boolean>) {
        var adLoadResult by remember { mutableStateOf(true) }
        if (!adLoadResult) {
            return
        }
        //初始化AdMod
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(IntrinsicSize.Min),
        ) {
            AndroidView(
                modifier = Modifier
                    .width(320.dp)
                    .height(50.dp),
                factory = { context ->
                    AdView(context).apply {
                        this.adSize = AdSize.BANNER
                        this.adUnitId = ADMOB_ID
                        loadAd(adRequest)
                        this.adListener = object : AdListener() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Logger.w("load ads failed, detail: $loadAdError")
                                adLoadResult = false
                            }
                        }
                    }
                }
            )
            IconButton(modifier = Modifier.fillMaxHeight(), onClick = {
                adsDialogShowState.value = true
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_why_show_ad),
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    fun BuildAdDialog(adsDialogShowState: MutableState<Boolean>) {
        if (!adsDialogShowState.value) return
        AlertDialog(
            onDismissRequest = { adsDialogShowState.value = false },
            confirmButton = {
                TextButton(onClick = { adsDialogShowState.value = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            title = {
                Text(text = stringResource(R.string.action_why_ad))
            }, text = {
                Text(text = stringResource(R.string.hint_why_ads_appear))
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
                BuildResultItem(history = item)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun BuildResultItem(history: AnimationHistory) {
        val similarity = "${DecimalFormat("#.0000").format(history.similarity * 100)}%"
        val isOldData =
            history.episode == "old" || history.similarity == 0.0
        Card(
            modifier = Modifier.padding(horizontal = 8.dp),
            border = BorderStroke(
                1.dp,
                colorResource(R.color.outlined_stroke_color)
            ),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                if (isOldData) {
                    getString(R.string.hint_data_convert_no_detail_in_history).toastLong()
                } else {
                    DetailActivity.showDetail(
                        this@HistoryActivity,
                        File(history.cachePath), history.originPath, history.title
                    )
                }
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
