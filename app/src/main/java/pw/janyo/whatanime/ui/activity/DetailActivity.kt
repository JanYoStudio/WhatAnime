package pw.janyo.whatanime.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.ads.*
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.connectServer
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.config.toCustomTabs
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.model.ShowImage
import pw.janyo.whatanime.ui.state.AlertDialog
import pw.janyo.whatanime.ui.state.DialogShowState
import pw.janyo.whatanime.ui.state.observerAsShowState
import pw.janyo.whatanime.ui.state.rememberDialogShowState
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.viewModel.DetailViewModel
import vip.mystery0.tools.utils.formatTime
import java.io.File
import java.text.DecimalFormat

class DetailActivity : BaseComposeActivity<DetailViewModel>() {
    override val viewModel: DetailViewModel by viewModel()

    private val imageLoader by lazy {
        ImageLoader.Builder(this)
            .placeholder(R.mipmap.janyo_studio)
            .error(R.mipmap.load_failed)
            .build()
    }

    companion object {
        private const val INTENT_CACHE_FILE = "INTENT_CACHE_FILE"
        private const val INTENT_ORIGIN_PATH = "INTENT_ORIGIN_PATH"
        private const val INTENT_TITLE = "INTENT_TITLE"

        fun showDetail(context: Context, cacheFile: File, originPath: String, title: String) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(INTENT_CACHE_FILE, cacheFile)
            intent.putExtra(INTENT_ORIGIN_PATH, originPath)
            intent.putExtra(INTENT_TITLE, title)
            context.startActivity(intent)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun initIntent() {
        //查看历史记录
        val cacheFile: File = intent.getSerializableExtra(INTENT_CACHE_FILE) as File
        val originPath = intent.getStringExtra(INTENT_ORIGIN_PATH)!!
        //加载显示历史记录中的缓存文件
        val showImage = ShowImage()
        showImage.mimeType = ""
        showImage.originPath = originPath
        showImage.cachePath = cacheFile.absolutePath
        viewModel.imageFile.postValue(showImage)
        //设置标题
        title = intent.getStringExtra(INTENT_TITLE)
        val originFile = File(originPath)
        viewModel.search(
            originFile,
            null,
            cacheFile.absolutePath,
            originPath,
            "",
            connectServer
        )
    }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val errorMessage by viewModel.errorMessageData.observeAsState()
        val adsDialogShowState = rememberDialogShowState<Boolean>(null)
        val showFloatDialog by viewModel.showFloatDialog.observeValueAsState()
        WhatAnimeTheme {
            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        title = { Text(text = title.toString()) },
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        navigationIcon = {
                            IconButton(onClick = {
                                finish()
                            }) {
                                Icon(Icons.Filled.ArrowBack, "")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                viewModel.changeFloatDialogVisibility()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_preview),
                                    contentDescription = "",
                                    tint = MaterialTheme.colors.onPrimary
                                )
                            }
                        }
                    )
                },
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    if (inBlackList) {
                        BuildAdLayout(adsDialogShowState)
                    }
                    BuildList()

                    Crossfade(
                        targetState = showFloatDialog,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        if (it) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                elevation = 4.dp,
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(8.dp),
                                ) {
                                    val mediaSource by viewModel.mediaSource.observeAsState()
                                    if (mediaSource == null) {
                                        BuildImage()
                                    } else {
                                        BuildPlayer(mediaSource)
                                    }
                                    val loadingVideo by viewModel.loadingVideo.observeValueAsState()
                                    if (loadingVideo) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .width(320.dp)
                                                .height(180.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            BuildAdDialog(adsDialogShowState)
            BuildAlertDialog()
            errorMessage?.let {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
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

    @Composable
    fun BuildAlertDialog() {
        AlertDialog(
            dialogShowState = viewModel.clickDocs.observerAsShowState(this),
            title = {
                Text(
                    text = stringResource(
                        id = R.string.hint_show_animation_detail,
                        requiredData().title_native ?: ""
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        toCustomTabs("https://anilist.co/anime/${requiredData().anilist_id}")
                        dismiss()
                    }
                ) {
                    Text(stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { dismiss() }) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        )
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
    fun BuildImage() {
        val imageFile by viewModel.imageFile.observeAsState()
        val painter = if (imageFile != null) {
            val originFile = File(imageFile!!.originPath)
            //判断图片文件是否存在
            if (!originFile.exists()) {
                //如果不存在，显示错误信息
                viewModel.errorMessageState(stringResource(id = R.string.hint_select_file_not_exist))
                return
            }
            rememberCoilPainter(request = originFile)
        } else {
            painterResource(R.mipmap.janyo_studio)
        }
        Image(
            painter = painter, contentDescription = null,
            modifier = Modifier
                .width(320.dp)
                .height(180.dp)
        )
    }

    @Composable
    fun BuildPlayer(mediaSource: MediaSource?) {
        mediaSource?.let {
            AndroidView(modifier = Modifier
                .width(320.dp)
                .height(180.dp),
                factory = { context ->
                    PlayerView(context).apply {
                        val exoPlayer = SimpleExoPlayer.Builder(context).build()
                        exoPlayer.addListener(object : Player.Listener {
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
                                }
                            }

                            override fun onPlayerError(error: ExoPlaybackException) {
                                super.onPlayerError(error)
                                Logger.e("onPlayerError: ", error)
                                viewModel.loadingVideo.postValue(false)
                                viewModel.mediaSource.postValue(null)
                                error.toastLong()
                            }
                        })
                        this.player = exoPlayer
                        this.useController = false
                        exoPlayer.clearMediaItems()
                        exoPlayer.setMediaSource(it)
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
                })
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun BuildList() {
        val list by viewModel.resultList.observeAsState()
        list?.let {
            if (it.isEmpty()) {
                viewModel.errorMessageState(stringResource(id = R.string.hint_no_result))
                return
            }

            LazyColumn(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(it) { item: Docs ->
                    BuildResultItem(docs = item)
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun BuildResultItem(docs: Docs) {
        val requestUrl = String.format(
            Constant.previewUrl,
            docs.anilist_id,
            Uri.encode(docs.filename),
            docs.at,
            docs.tokenthumb ?: ""
        )
        Card(
            modifier = Modifier.padding(horizontal = 8.dp),
            border = BorderStroke(
                1.dp,
                colorResource(id = R.color.outlined_stroke_color)
            ),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                viewModel.clickDocs.postValue(docs)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (docs.similarity < 0.87) {
                    Text(
                        text = stringResource(id = R.string.hint_probably_mistake),
                        color = MaterialTheme.colors.secondary,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                Row {
                    Column {
                        BuildText(stringResource(id = R.string.hint_title_native))
                        BuildText(stringResource(id = R.string.hint_title_chinese))
                        BuildText(stringResource(id = R.string.hint_title_english))
                        BuildText(stringResource(id = R.string.hint_title_romaji))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        BuildText(docs.title_native ?: "")
                        BuildText(docs.title_chinese ?: "")
                        BuildText(docs.title_english ?: "")
                        BuildText(docs.title_romaji ?: "")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberCoilPainter(
                            request = requestUrl,
                            imageLoader = imageLoader
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .height(90.dp)
                            .width(160.dp)
                            .clickable(onClick = {
                                viewModel.playVideo(docs)
                            })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        BuildText(stringResource(id = R.string.hint_time))
                        BuildText(stringResource(id = R.string.hint_episode))
                        BuildText(stringResource(id = R.string.hint_ani_list_id))
                        BuildText(stringResource(id = R.string.hint_mal_id))
                        BuildText(stringResource(id = R.string.hint_similarity))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        BuildText((docs.at.toLong() * 1000).formatTime())
                        BuildText("${docs.episode}")
                        BuildText("${docs.anilist_id}")
                        BuildText("${docs.mal_id}")
                        BuildText("${DecimalFormat("#.000").format(docs.similarity * 100)}%")
                    }
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