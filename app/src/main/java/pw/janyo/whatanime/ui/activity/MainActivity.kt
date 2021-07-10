package pw.janyo.whatanime.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.ads.*
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.config.inChina
import pw.janyo.whatanime.config.toCustomTabs
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.constant.Constant.ADMOB_ID
import pw.janyo.whatanime.model.Result
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.utils.firstNotNull
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.tools.factory.toJson
import vip.mystery0.tools.utils.formatTime
import java.io.File
import java.text.DecimalFormat

class MainActivity : BaseComposeActivity<MainViewModel>() {
    override val viewModel: MainViewModel by viewModel()

    private val selectIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val type = contentResolver.getType(it.data!!.data!!)
                if (type.isNullOrBlank()) {
                    R.string.hint_select_file_not_exist.asString().toast()
                } else {
                    viewModel.parseImageFile(it.data!!, type)
                }
            }
        }
    private val imageLoader by lazy {
        ImageLoader.Builder(this)
            .placeholder(R.mipmap.janyo_studio)
            .error(R.mipmap.load_failed)
            .build()
    }
    private val exoPlayer: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            viewModel.loadingVideo.postValue(true)
                        }
                        Player.STATE_READY -> {
                            viewModel.loadingVideo.postValue(false)
                        }
                        Player.STATE_ENDED -> {
                            viewModel.mediaSource.postValue(null)
                        }
                        Player.STATE_IDLE -> {
                        }
                    }
                }

                override fun onPlayerError(error: ExoPlaybackException) {
                    super.onPlayerError(error)
                    Logger.e("onPlayerError: ", error)
                    viewModel.loadingVideo.postValue(false)
                    viewModel.mediaSource.postValue(null)
                    viewModel.errorMessageState(
                        firstNotNull(
                            getString(R.string.hint_unknow_error),
                            error.cause?.message,
                            error.message,
                        )
                    )
                }
            })
        }
    }

    companion object {
        private const val INTENT_URI = "INTENT_URI"
        private const val INTENT_MIME_TYPE = "INTENT_MIME_TYPE"

        fun receiveShare(context: Context, uri: Uri, mimeType: String) {
            Logger.i("receiveShare: uri: $uri, mimeType: $mimeType")
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                putExtra(INTENT_URI, uri)
                putExtra(INTENT_MIME_TYPE, mimeType)
            })
        }
    }

    @SuppressLint("RestrictedApi")
    override fun initIntent() {
        if (intent.hasExtra(INTENT_URI)) {
            //接收其他来源的图片
            try {
                val uri = intent.getParcelableExtra<Uri>(INTENT_URI)
                intent.data = uri
                viewModel.parseImageFile(intent, intent.getStringExtra(INTENT_MIME_TYPE)!!)
            } catch (e: Exception) {
                R.string.hint_select_file_path_null.asString().toast()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showQuota()
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val isRefreshing by viewModel.refreshData.observeValueAsState()
        val adLoadResult = remember { mutableStateOf(true) }
        val adsDialogShowState = remember { mutableStateOf(false) }
        val resultList by viewModel.resultList.observeAsState()
        WhatAnimeTheme {
            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            Scaffold(
                scaffoldState = scaffoldState,
                bottomBar = {
                    BottomAppBar(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        cutoutShape = CircleShape
                    ) {
                        IconButton(onClick = {
                            toCustomTabs(if (inChina == true) Constant.indexVipUrl else Constant.indexAppUrl)
                        }) {
                            Icon(
                                imageVector = Icons.TwoTone.Info,
                                contentDescription = stringResource(R.string.action_about),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        IconButton(onClick = {
                            toCustomTabs(if (inChina == true) Constant.faqVipUrl else Constant.faqAppUrl)
                        }) {
                            Icon(
                                imageVector = Icons.TwoTone.HelpCenter,
                                contentDescription = stringResource(R.string.action_faq),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        IconButton(onClick = {
                            intentTo(HistoryActivity::class)
                        }) {
                            Icon(
                                imageVector = Icons.TwoTone.Plagiarism,
                                contentDescription = stringResource(R.string.action_history),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        IconButton(onClick = {
                            intentTo(AboutActivity::class)
                        }) {
                            Icon(
                                imageVector = Icons.TwoTone.Settings,
                                contentDescription = stringResource(R.string.action_settings),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        if (inBlackList && adLoadResult.value) {
                            IconButton(onClick = {
                                adsDialogShowState.value = true
                            }) {
                                Icon(
                                    imageVector = Icons.TwoTone.ContactSupport,
                                    contentDescription = stringResource(R.string.action_why_ad),
                                    tint = MaterialTheme.colors.onPrimary
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*"
                        )
                        selectIntent.launch(intent)
                    }) {
                        Icon(
                            imageVector = Icons.TwoTone.ImageSearch,
                            contentDescription = null
                        )
                    }
                },
                isFloatingActionButtonDocked = true,
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(vertical = 8.dp)
                ) {
                    if (inBlackList) {
                        BuildAdLayout(adLoadResult, adsDialogShowState)
                    }
                    BuildList(resultList)
                }
            }
            BuildAdDialog(adsDialogShowState)
            BuildRefreshDialog(isRefreshing)
            BuildAlertDialog()
            BuildVideoDialog()
            observerErrorMessage {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            }
        }
    }

    @Composable
    fun BuildAdLayout(
        adLoadResult: MutableState<Boolean>,
        adsDialogShowState: MutableState<Boolean>
    ) {
        if (!adLoadResult.value) {
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
                                adLoadResult.value = false
                            }
                        }
                    }
                }
            )
            IconButton(modifier = Modifier.fillMaxHeight(), onClick = {
                adsDialogShowState.value = true
            }) {
                Icon(
                    imageVector = Icons.TwoTone.ContactSupport,
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    fun BuildRefreshDialog(isRefreshing: Boolean) {
        if (!isRefreshing) return
        Dialog(
            onDismissRequest = { viewModel.refreshState(false) },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp),
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Loading...", color = MaterialTheme.colors.onBackground)
                }
            }
        }
    }

    @Composable
    fun BuildAlertDialog() {
        val clickDocs by viewModel.clickDocs.observeAsState()
        clickDocs?.let {
            AlertDialog(
                onDismissRequest = { viewModel.clickDocs.postValue(null) },
                title = {
                    Text(
                        text = stringResource(
                            R.string.hint_show_animation_detail,
                            firstNotNull(
                                "",
                                it.anilist.title?.native,
                                it.anilist.title?.english,
                                it.anilist.title?.romaji,
                                it.anilist.synonyms?.toTypedArray(),
                            )
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            toCustomTabs("https://anilist.co/anime/${it.anilist.id}")
                            viewModel.clickDocs.postValue(null)
                        }
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.clickDocs.postValue(null) }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }
    }

    @Composable
    fun BuildVideoDialog() {
        val mediaSource by viewModel.mediaSource.observeAsState()
        mediaSource?.let {
            Dialog(onDismissRequest = {
                exoPlayer.stop()
                viewModel.mediaSource.postValue(null)
            }, content = {
                Box(modifier = Modifier.padding(8.dp)) {
                    AndroidView(modifier = Modifier
                        .width(480.dp)
                        .height(270.dp),
                        factory = { context ->
                            PlayerView(context).apply {
                                this.player = exoPlayer
                                this.useController = false
                                exoPlayer.clearMediaItems()
                                exoPlayer.setMediaSource(it)
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            }
                        })
                    val loadingVideo by viewModel.loadingVideo.observeValueAsState()
                    if (loadingVideo) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            })
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

    @Composable
    fun BuildImage() {
        val imageFile by viewModel.imageFile.observeAsState()
        val painter = if (imageFile != null) {
            val originFile = File(imageFile!!.originPath)
            //判断图片文件是否存在
            if (!originFile.exists()) {
                //如果不存在，显示错误信息
                viewModel.errorMessageState(stringResource(R.string.hint_select_file_not_exist))
                return
            }
            rememberCoilPainter(request = originFile)
        } else {
            painterResource(R.mipmap.janyo_studio)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painter, contentDescription = null,
                modifier = Modifier
                    .width(320.dp)
                    .height(180.dp)
                    .padding(8.dp)
            )
        }
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun BuildList(resultList: List<Result>?) {
        val searchQuota by viewModel.quota.observeAsState()
        if (resultList != null && resultList.isEmpty()) {
            viewModel.errorMessageState(stringResource(R.string.hint_no_result))
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(8.dp),
                    border = BorderStroke(
                        2.dp,
                        colorResource(R.color.outlined_stroke_color)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BuildImage()
                        searchQuota?.let {
                            Text(
                                modifier = Modifier
                                    .padding(8.dp),
                                text = stringResource(R.string.hint_search_quota) + "${it.quotaUsed}/${it.quota}",
                                color = MaterialTheme.colors.onSurface,
                            )
                        }
                    }
                }
            }
            resultList?.let {
                items(it) { item: Result ->
                    BuildResultItem(result = item)
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun BuildResultItem(result: Result) {
        Card(
            modifier = Modifier.padding(horizontal = 8.dp),
            border = BorderStroke(
                1.dp,
                colorResource(R.color.outlined_stroke_color)
            ),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                debugOnClick("将会打开地址 https://anilist.co/anime/${result.anilist.id}") {
                    viewModel.clickDocs.postValue(result)
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (result.similarity < 0.87) {
                    Text(
                        text = stringResource(R.string.hint_probably_mistake),
                        color = MaterialTheme.colors.secondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                SelectionContainer {
                    Row {
                        Column {
                            BuildText(stringResource(R.string.hint_title_native), FontWeight.Bold)
                            BuildText(stringResource(R.string.hint_title_english))
                            BuildText(stringResource(R.string.hint_title_romaji))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            BuildText(result.anilist.title?.native ?: "", FontWeight.Bold)
                            BuildText(result.anilist.title?.english ?: "")
                            BuildText(result.anilist.title?.romaji ?: "")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberCoilPainter(
                            request = result.image,
                            imageLoader = imageLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .height(90.dp)
                            .width(160.dp)
                            .clickable(onClick = {
                                debugOnClick("""
                                    图片地址为 ${result.image}
                                    将会播放视频 ${result.video}&size=l
                                """.trimIndent()){
                                    viewModel.playVideo(result)
                                }
                            })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SelectionContainer {
                        Row {
                            Column {
                                BuildText(stringResource(R.string.hint_time))
                                BuildText(stringResource(R.string.hint_episode))
                                BuildText(stringResource(R.string.hint_ani_list_id))
                                BuildText(stringResource(R.string.hint_mal_id))
                                BuildText(stringResource(R.string.hint_similarity), FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                BuildText("${(result.from.toLong() * 1000).formatTime()} ~ ${(result.to.toLong() * 1000).formatTime()}")
                                BuildText(result.episode ?: "")
                                BuildText("${result.anilist.id}")
                                BuildText("${result.anilist.idMal}")
                                BuildText(
                                    "${DecimalFormat("#.000").format(result.similarity * 100)}%",
                                    FontWeight.Bold
                                )
                            }
                        }
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
