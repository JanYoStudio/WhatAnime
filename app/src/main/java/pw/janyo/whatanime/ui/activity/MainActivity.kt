package pw.janyo.whatanime.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.config.inChina
import pw.janyo.whatanime.config.toCustomTabs
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.ui.state.AlertDialog
import pw.janyo.whatanime.ui.state.DialogShowState
import pw.janyo.whatanime.ui.state.observerAsShowState
import pw.janyo.whatanime.ui.state.rememberDialogShowState
import pw.janyo.whatanime.ui.theme.WhatAnimeTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.viewModel.MainViewModel
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

    companion object {
        private const val INTENT_URI = "INTENT_URI"
        private const val INTENT_MIME_TYPE = "INTENT_MIME_TYPE"

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showQuota()
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val errorMessage by viewModel.errorMessageData.observeAsState()
        val isRefreshing by viewModel.refreshData.observeValueAsState()
        val adLoadResult = remember { mutableStateOf(true) }
        val adsDialogShowState = rememberDialogShowState<Boolean>(null)
        val searchQuota by viewModel.quota.observeAsState()
        val resultList by viewModel.resultList.observeAsState()
        WhatAnimeTheme {
            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            Scaffold(
                scaffoldState = scaffoldState,
                bottomBar = {
                    BottomAppBar(
                        cutoutShape = CircleShape
                    ) {
                        IconButton(onClick = {
                            toCustomTabs(if (inChina == true) Constant.indexVipUrl else Constant.indexAppUrl)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_about),
                                contentDescription = stringResource(id = R.string.action_about),
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                        IconButton(onClick = {
                            toCustomTabs(if (inChina == true) Constant.faqVipUrl else Constant.faqAppUrl)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_faq),
                                contentDescription = stringResource(id = R.string.action_faq),
                                tint = MaterialTheme.colors.onPrimary
                            )
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
                        if (!adLoadResult.value) {
                            IconButton(onClick = {
                                adsDialogShowState.show(true)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_why_show_ad),
                                    contentDescription = stringResource(id = R.string.action_why_ad),
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
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null
                        )
                    }
                },
                isFloatingActionButtonDocked = true,
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    if (inBlackList) {
                        BuildAdLayout(adLoadResult, adsDialogShowState)
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .animateContentSize(),
                        border = BorderStroke(
                            2.dp,
                            colorResource(id = R.color.outlined_stroke_color)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 0.dp,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val mediaSource by viewModel.mediaSource.observeAsState()
                                if (mediaSource == null) {
                                    BuildImage()
                                } else {
                                    BuildPlayer(mediaSource)
                                }
                                val loadingVideo by viewModel.loadingVideo.observeValueAsState()
                                if (loadingVideo) {
                                    CircularProgressIndicator()
                                }
                            }
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
                    BuildList(resultList)
                }
            }
            BuildAdDialog(adsDialogShowState)
            BuildRefreshDialog(isRefreshing)
            BuildAlertDialog()
            errorMessage?.let {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(it)
                }
            }
        }
    }

    @Composable
    fun BuildAdLayout(
        adLoadResult: MutableState<Boolean>,
        adsDialogShowState: DialogShowState<Boolean>
    ) {
        if (!adLoadResult.value) {
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
                                adLoadResult.value = false
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
    fun BuildRefreshDialog(isRefreshing: Boolean) {
        if (!isRefreshing) return
        Dialog(
            onDismissRequest = { viewModel.refreshState(false) },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Loading...", color = MaterialTheme.colors.onBackground)
                }
            }
        }
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

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun BuildList(resultList: List<Docs>?) {
        if (resultList != null && resultList.isEmpty()) {
            viewModel.errorMessageState(stringResource(id = R.string.hint_no_result))
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            resultList?.let {
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
