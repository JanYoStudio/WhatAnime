package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpCenter
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Plagiarism
import androidx.compose.material.icons.outlined.AppShortcut
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.toCustomTabs
import pw.janyo.whatanime.ui.activity.contract.ImagePickResultContract
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.firstNotBlank
import pw.janyo.whatanime.utils.formatTime
import pw.janyo.whatanime.viewModel.MainViewModel
import java.io.File
import java.text.DecimalFormat

class MainActivity : BaseComposeActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val INTENT_URI = "INTENT_URI"
        private const val INTENT_MIME_TYPE = "INTENT_MIME_TYPE"

        fun receiveShare(uri: Uri, mimeType: String): Intent.() -> Unit {
            Log.d(TAG, "receiveShare: uri: $uri")
            return {
                putExtra(INTENT_URI, uri)
                putExtra(INTENT_MIME_TYPE, mimeType)
            }
        }
    }

    private val viewModel: MainViewModel by viewModels()

    private val imageSelectLauncher =
        registerForActivityResult(ImagePickResultContract()) { intent ->
            if (intent == null) {
                return@registerForActivityResult
            }
            val type = contentResolver.getType(intent.data!!)
            if (type.isNullOrBlank()) {
                R.string.hint_select_file_not_exist.toast()
            } else {
                viewModel.searchImageFile(intent, type)
            }
        }
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            viewModel.loadPlaying(true)
                        }

                        Player.STATE_READY -> {
                            viewModel.loadPlaying(false)
                        }

                        Player.STATE_ENDED -> {
                            viewModel.playDone()
                        }

                        Player.STATE_IDLE -> {
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    viewModel.playError(error)
                }
            })
        }
    }

    override fun initIntent() {
        if (intent.hasExtra(INTENT_URI)) {
            //接收其他来源的图片
            try {
                @Suppress("DEPRECATION")
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(INTENT_URI, Uri::class.java)
                } else {
                    intent?.getParcelableExtra(INTENT_URI)
                }
                val mimeType = intent?.getStringExtra(INTENT_MIME_TYPE)
                intent.data = uri
                viewModel.searchImageFile(intent, mimeType!!)
            } catch (e: Exception) {
                R.string.hint_select_file_path_null.toast()
            }
        }
        installSplashScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showQuota()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val listState by viewModel.listState.collectAsState()
        val showChineseTitle by viewModel.showChineseTitle.collectAsState()
        val cutBorders by viewModel.cutBorders.collectAsState()

        val animeDialogState = remember { mutableStateOf<SearchAnimeResultItem?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.width(56.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_whatanime),
                                contentDescription = "logo",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = title.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .padding(start = 56.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val quota by viewModel.searchQuota.collectAsState()
                        Text(
                            text = stringResource(R.string.hint_quota_used, quota.quotaUsed),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = stringResource(R.string.hint_quota_total, quota.quota))
                    }
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    NavigationDrawerItem(
                        icon = { Icons(Icons.Filled.Plagiarism) },
                        label = { Text(stringResource(id = R.string.action_history)) },
                        selected = false,
                        onClick = {
                            intentTo(HistoryActivity::class)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icons(Icons.Outlined.Settings) },
                        label = { Text(stringResource(id = R.string.action_settings)) },
                        selected = false,
                        onClick = {
                            intentTo(SettingsActivity::class)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icons(Icons.Outlined.AppShortcut) },
                        label = { Text(stringResource(id = R.string.action_cut_border)) },
                        badge = {
                            Switch(checked = cutBorders, onCheckedChange = {
                                viewModel.changeCutBorders()
                            })
                        },
                        selected = true,
                        onClick = {
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icons(Icons.AutoMirrored.Outlined.HelpCenter) },
                        label = { Text(stringResource(id = R.string.action_faq)) },
                        selected = false,
                        onClick = {
                            toCustomTabs(Constant.faqUrl)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        icon = { Icons(Icons.Filled.AutoAwesome) },
                        label = { Text(stringResource(id = R.string.settings_group_about)) },
                        selected = false,
                        onClick = {
                            intentTo(AboutActivity::class)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    Spacer(modifier = Modifier.weight(1F))
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            modifier = Modifier
                                .weight(1F),
                            onClick = {
                                toCustomTabs(Constant.janYoStudioUrl)
                            }) {
                            Text(
                                text = stringResource(id = R.string.action_about_janyo),
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .size(4.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                        ) {}
                        TextButton(
                            modifier = Modifier
                                .weight(1F),
                            onClick = {
                                toCustomTabs(Constant.whatAnimeUrl)
                            }) {
                            Text(
                                text = stringResource(id = R.string.action_about_whatanime),
                            )
                        }
                    }
                }
            },
            content = {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_whatanime),
                                        contentDescription = "logo",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = title.toString(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icons(Icons.Outlined.Menu)
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    imageSelectLauncher.launch("image/*")
                                }) {
                                    Icons(Icons.Outlined.ImageSearch)
                                }
                            }
                        )
                    },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(vertical = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .matchParentSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                item {
                                    Card(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clickable {
                                                if (!listState.loading && listState.list.isEmpty()) {
                                                    imageSelectLauncher.launch("image/*")
                                                }
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                    ) {
                                        BuildImage(listState.searchImageFile)
                                    }
                                }
                                when {
                                    listState.list.isEmpty() -> {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .height(640.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    modifier = Modifier
                                                        .width(172.dp),
                                                    fontSize = 16.sp,
                                                    text = stringResource(id = R.string.hint_select_to_search),
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                        }
                                    }

                                    else -> {
                                        items(listState.list) {
                                            BuildResultItem(
                                                it,
                                                showChineseTitle,
                                                animeDialogState
                                            ) {
                                                viewModel.playVideo(it)
                                            }
                                        }
                                    }
                                }
                            }
                            Crossfade(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                targetState = listState.list.size,
                                label = "image search button",
                            ) {
                                if (it == 0) {
                                    ExtendedFloatingActionButton(
                                        text = {
                                            Text(text = stringResource(id = R.string.action_start_search))
                                        },
                                        icon = {
                                            Icons(Icons.Outlined.ImageSearch)
                                        },
                                        onClick = {
                                            imageSelectLauncher.launch("image/*")
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )
        ShowProgressDialog(
            show = listState.loading,
            text = stringResource(id = R.string.hint_searching)
        )
        BuildAlertDialog(animeDialogState)
        BuildVideoDialog()

        if (listState.errorMessage.isNotBlank()) {
            LaunchedEffect(key1 = "errorMessage") {
                snackbarHostState.showSnackbar(listState.errorMessage)
            }
        }
    }

    @Composable
    fun BuildAlertDialog(animeDialogState: MutableState<SearchAnimeResultItem?>) {
        if (animeDialogState.value == null) return
        val item = animeDialogState.value!!
        AlertDialog(
            onDismissRequest = { animeDialogState.value = null },
            title = {
                Text(
                    text = stringResource(
                        R.string.hint_show_animation_detail,
                        firstNotBlank(
                            "",
                            ArrayList<String?>().apply {
                                add(item.aniList.title.native)
                                add(item.aniList.title.english)
                                add(item.aniList.title.romaji)
                                addAll(item.aniList.synonyms)
                            },
                        )
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        toCustomTabs("https://anilist.co/anime/${item.aniList.id}")
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
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun BuildVideoDialog() {
        val mediaSource by viewModel.playMediaSource.collectAsState()
        mediaSource?.let {
            Dialog(onDismissRequest = {
                exoPlayer.stop()
                viewModel.playDone()
            }, content = {
                Box(modifier = Modifier.padding(8.dp)) {
                    AndroidView(modifier = Modifier
                        .width(480.dp)
                        .height(270.dp),
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer
                                this.useController = false
                                exoPlayer.clearMediaItems()
                                exoPlayer.setMediaSource(it)
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            }
                        })
                    val loadingVideo by viewModel.playLoading.collectAsState()
                    if (loadingVideo) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            })
        }
    }

    @Composable
    fun BuildImage(searchImageFile: File?) {
        var data: Any = searchImageFile ?: R.mipmap.janyo_studio
        searchImageFile?.let {
            if (!it.exists()) {
                R.string.hint_select_file_not_exist.toast(true)
                data = R.mipmap.janyo_studio
            }
        }
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(320.dp)
                .height(180.dp)
                .padding(8.dp),
        )
    }
}

@Composable
fun BuildResultItem(
    result: SearchAnimeResultItem,
    showChineseTitle: Boolean,
    animeDialogState: MutableState<SearchAnimeResultItem?>,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable {
                animeDialogState.value = result
            },
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            SelectionContainer {
                Row {
                    Column {
                        val nativeTitle = result.aniList.title.native ?: ""
                        val chineseTitle = result.aniList.title.chinese ?: ""
                        val englishTitle = result.aniList.title.english ?: ""
                        val romajiTitle = result.aniList.title.romaji ?: ""
                        if (nativeTitle.isNotBlank()) {
                            BuildText(
                                text = stringResource(
                                    R.string.detail_hint_native_title,
                                    nativeTitle
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        if (showChineseTitle && chineseTitle.isNotBlank()) {
                            BuildText(
                                stringResource(R.string.detail_hint_chinese_title, chineseTitle),
                                FontWeight.Bold
                            )
                        }
                        if (englishTitle.isNotBlank()) {
                            BuildText(
                                stringResource(
                                    R.string.detail_hint_english_title,
                                    englishTitle
                                )
                            )
                        }
                        if (romajiTitle.isNotBlank()) {
                            BuildText(
                                stringResource(
                                    R.string.detail_hint_romaji_title,
                                    romajiTitle
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(result.image)
                        .build(),
                    imageLoader = ImageLoader.Builder(LocalContext.current)
                        .placeholder(R.mipmap.janyo_studio)
                        .error(R.mipmap.load_failed)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .height(90.dp)
                        .width(160.dp)
                        .clickable(onClick = {
                            onClick()
                        })
                )
                Spacer(modifier = Modifier.width(8.dp))
                SelectionContainer {
                    Row {
                        Column {
                            BuildText(stringResource(R.string.detail_hint_time))
                            BuildText(stringResource(R.string.detail_hint_ani_list_id))
                            BuildText(stringResource(R.string.detail_hint_my_anime_list_id))
                            BuildText(
                                stringResource(R.string.detail_hint_similarity),
                                FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val similarityColor = if (result.similarity < 0.9) {
                            Color.Red
                        } else {
                            Color.Green
                        }
                        Column {
                            BuildText("${(result.from.toLong() * 1000).formatTime()} ~ ${(result.to.toLong() * 1000).formatTime()}")
                            BuildText("${result.aniList.id}")
                            BuildText("${result.aniList.idMal}")
                            BuildText(
                                text = "${DecimalFormat("#.000").format(result.similarity * 100)}%",
                                fontWeight = FontWeight.Bold,
                                textColor = similarityColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuildText(
    text: String,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = 12.sp,
    textColor: Color = Color.Unspecified
) {
    Text(
        text = text,
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        color = textColor,
    )
}
