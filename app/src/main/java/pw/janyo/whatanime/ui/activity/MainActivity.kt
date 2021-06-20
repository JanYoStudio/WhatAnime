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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.gms.ads.*
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.connectServer
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.config.inChina
import pw.janyo.whatanime.config.toCustomTabs
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.model.ShowImage
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.tools.utils.formatTime
import java.io.File
import java.text.DecimalFormat

class MainActivity : BaseComposeActivity<MainViewModel>() {
    override val viewModel: MainViewModel by viewModel()

    private var homePage = true
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

    companion object {
        private const val INTENT_CACHE_FILE = "INTENT_CACHE_FILE"
        private const val INTENT_ORIGIN_PATH = "INTENT_ORIGIN_PATH"
        private const val INTENT_TITLE = "INTENT_TITLE"
        private const val INTENT_URI = "INTENT_URI"
        private const val INTENT_MIME_TYPE = "INTENT_MIME_TYPE"

        fun showDetail(context: Context, cacheFile: File, originPath: String, title: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_CACHE_FILE, cacheFile)
            intent.putExtra(INTENT_ORIGIN_PATH, originPath)
            intent.putExtra(INTENT_TITLE, title)
            context.startActivity(intent)
        }

        fun receiveShare(context: Context, uri: Uri, mimeType: String) {
            Logger.i("receiveShare: uri: $uri, mimeType: $mimeType")
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_URI, uri)
            intent.putExtra(INTENT_MIME_TYPE, mimeType)
            context.startActivity(intent)
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
        if (intent.hasExtra(INTENT_CACHE_FILE) && intent.hasExtra(INTENT_TITLE)) {
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
            homePage = false
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
        viewModel.errorMessageState("测试Snackbar")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (homePage) {
            viewModel.showQuota()
        }
    }

    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val adLoadResult = remember { mutableStateOf(true) }
        val showAdsDialog = remember { mutableStateOf(false) }
        WhatAnimeTheme {
            var menuActions: @Composable RowScope.() -> Unit = {}
            if (homePage) {
                menuActions = {

                }
            }
            val navigationIcon: @Composable (() -> Unit)? = if (!homePage) {
                {
                    IconButton(onClick = {
                        finish()
                    }) {
                        Icon(Icons.Filled.ArrowBack, "")
                    }
                }
            } else null
            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            Scaffold(
                scaffoldState = scaffoldState,
//                topBar = {
//                    TopAppBar(
//                        title = { Text(text = title.toString()) },
//                        backgroundColor = MaterialTheme.colors.primary,
//                        contentColor = MaterialTheme.colors.onPrimary,
//                        actions = menuActions,
//                        navigationIcon = navigationIcon,
//                    )
//                },
                bottomBar = {
                    BottomAppBar(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.onPrimary,
                        cutoutShape = CircleShape
                    ) {
                        var showDropMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = {
                            showDropMenu = true
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_more),
                                contentDescription = stringResource(id = R.string.action_more),
                                tint = MaterialTheme.colors.onPrimary
                            )
                            DropdownMenu(
                                expanded = showDropMenu,
                                onDismissRequest = { showDropMenu = false }) {
                                DropdownMenuItem(onClick = {
                                    toCustomTabs(if (inChina == true) Constant.indexVipUrl else Constant.indexAppUrl)
                                    showDropMenu = false
                                }) {
                                    Text(text = stringResource(id = R.string.action_about))
                                }
                                DropdownMenuItem(onClick = {
                                    toCustomTabs(if (inChina == true) Constant.faqVipUrl else Constant.faqAppUrl)
                                    showDropMenu = false
                                }) {
                                    Text(text = stringResource(id = R.string.action_faq))
                                }
                                if (!adLoadResult.value) {
                                    DropdownMenuItem(onClick = {
                                        showAdsDialog.value = true
                                        showDropMenu = false
                                    }) {
                                        Text(text = stringResource(id = R.string.action_faq))
                                    }
                                }
                            }
                        }
                        IconButton(onClick = {
                            intentTo(HistoryActivity::class)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_history),
                                contentDescription = stringResource(id = R.string.action_history),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        IconButton(onClick = {
                            intentTo(AboutActivity::class)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = stringResource(id = R.string.action_settings),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (homePage) {
                        FloatingActionButton(onClick = {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.setDataAndType(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                "image/*"
                            )
                            selectIntent.launch(intent)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = null
                            )
                        }
                    }
                },
                isFloatingActionButtonDocked = true,
            ) {
                Column {
                    if (inBlackList) {
                        BuildAdLayout(adLoadResult, showAdsDialog)
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        border = BorderStroke(
                            1.dp,
                            colorResource(id = R.color.outlined_stroke_color)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = 2.dp,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(196.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val mediaSource by viewModel.mediaSource.observeAsState()
                                val loadingVideo by viewModel.loadingVideo.observeValueAsState()
                                if (mediaSource == null) {
                                    BuildImage()
                                } else {
                                    BuildPlayer(mediaSource)
                                }
                                if (loadingVideo) {
                                    CircularProgressIndicator()
                                }
                            }
                            val searchQuota by viewModel.quota.observeAsState()
                            searchQuota?.let {
                                val limit = if (it.limit == 0) it.user_limit else it.limit
                                val refreshTime =
                                    if (it.limit_ttl == 0) it.user_limit_ttl else it.limit_ttl
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .requiredHeight(IntrinsicSize.Min)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.hint_search_quota) + limit,
                                        color = MaterialTheme.colors.onSurface,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Divider(
                                        color = MaterialTheme.colors.onSurface,
                                        modifier = Modifier
                                            .width(1.dp)
                                            .fillMaxHeight()
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.hint_search_quota_ttl) + (refreshTime * 1000).toLong()
                                            .formatTime(),
                                        color = MaterialTheme.colors.onSurface,
                                    )
                                }
                            }
                        }
                    }
                    BuildList()
                }
                BuiltSnackbar(scope, scaffoldState)
            }
            BuildRefreshDialog()
            BuildAlertDialog()
        }
    }

    @Composable
    fun BuildAdLayout(adLoadResult: MutableState<Boolean>, showAdsDialog: MutableState<Boolean>) {
        //初始化AdMod
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        if (!adLoadResult.value) {
            return
        }
        if (showAdsDialog.value) {
            AlertDialog(onDismissRequest = {
                showAdsDialog.value = false
            }, title = {
                Text(text = stringResource(id = R.string.action_why_ad))
            }, text = {
                Text(text = stringResource(id = R.string.hint_why_ads_appear))
            }, confirmButton = {})
        }
        Row {
            AndroidView(
                modifier = Modifier.fillMaxWidth(), // Occupy the max size in the Compose UI tree
                factory = { context ->
                    AdView(context).apply {
                        this.adSize = AdSize.BANNER
                        this.adUnitId = "ca-app-pub-6114262658640635/9315758560"
                        loadAd(adRequest)
                        this.adListener = object : AdListener() {
                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                adLoadResult.value = false
                            }
                        }
                    }
                }
            )
            IconButton(onClick = {
                showAdsDialog.value = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_why_show_ad),
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    fun BuildRefreshDialog() {
        val isRefreshing by viewModel.refreshData.observeValueAsState()
        Logger.i("isRefreshing, $isRefreshing")
        if (!isRefreshing) return
        Dialog(
            onDismissRequest = { viewModel.refreshState(false) },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
                    .background(
                        MaterialTheme.colors.onBackground,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Loading...")
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
                            id = R.string.hint_show_animation_detail,
                            it.title_native ?: ""
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            toCustomTabs("https://anilist.co/anime/${it.anilist_id}")
                            viewModel.clickDocs.postValue(null)
                        }
                    ) {
                        Text(stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.clickDocs.postValue(null)
                        }
                    ) {
                        Text(stringResource(id = android.R.string.cancel))
                    }
                }
            )
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
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    fun BuildPlayer(mediaSource: MediaSource?) {
        mediaSource?.let {
            AndroidView(modifier = Modifier.fillMaxSize(),
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
            LazyColumn {
                it.forEach {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            border = BorderStroke(
                                1.dp,
                                colorResource(id = R.color.outlined_stroke_color)
                            ),
                            elevation = 2.dp,
                            shape = RoundedCornerShape(8.dp),
                            onClick = {
                                viewModel.clickDocs.postValue(it)
                            }
                        ) {
                            BuildResultItem(docs = it)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BuildResultItem(docs: Docs) {
        val requestUrl = String.format(
            Constant.previewUrl,
            docs.anilist_id,
            Uri.encode(docs.filename),
            docs.at,
            docs.tokenthumb ?: ""
        )
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
                    painter = rememberCoilPainter(request = requestUrl, imageLoader = imageLoader),
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

    @Composable
    fun BuiltSnackbar(scope: CoroutineScope, scaffoldState: ScaffoldState) {
        val errorMessage by viewModel.errorMessageData.observeAsState()
        errorMessage?.let {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(it)
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
