package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.twotone.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.launch
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.toCustomTabs
import pw.janyo.whatanime.ui.activity.contract.ImagePickResultContract
import pw.janyo.whatanime.utils.firstNotBlank
import pw.janyo.whatanime.utils.formatTime
import pw.janyo.whatanime.viewModel.MainViewModel
import java.io.File
import java.text.DecimalFormat

class MainActivity : BaseComposeActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val INTENT_URI = "INTENT_URI"

        fun receiveShare(uri: Uri): Intent.() -> Unit {
            Log.d(TAG, "receiveShare: uri: $uri")
            return {
                putExtra(INTENT_URI, uri)
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
                R.string.hint_select_file_not_exist.asString().toast()
            } else {
                viewModel.searchImageFile(intent)
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
                    Log.e(TAG, "onPlayerError: ")
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
                intent.data = uri
                viewModel.searchImageFile(intent)
            } catch (e: Exception) {
                R.string.hint_select_file_path_null.asString().toast()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.showQuota()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val listState by viewModel.listState.collectAsState()

        val animeDialogState = remember { mutableStateOf<SearchAnimeResultItem?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {

                }
            },
        ) {

        }
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            imageSelectLauncher.launch("image/*")
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.ImageSearch,
                                contentDescription = null
                            )
                        }
                    }
                )
//                TopAppBar(
//                    title = { Text(text = title.toString()) },
//                    actions = {
//                        IconButton(onClick = {
//                            intentTo(HistoryActivity::class)
//                        }) {
//                            Icon(
//                                imageVector = Icons.TwoTone.Plagiarism,
//                                contentDescription = stringResource(R.string.action_history),
//                            )
//                        }
//                        IconButton(onClick = {
//                            toCustomTabs(Constant.indexUrl)
//                        }) {
//                            Icon(
//                                imageVector = Icons.TwoTone.Info,
//                                contentDescription = stringResource(R.string.action_about)
//                            )
//                        }
//                        IconButton(onClick = {
//                            toCustomTabs(Constant.faqUrl)
//                        }) {
//                            Icon(
//                                imageVector = Icons.TwoTone.HelpCenter,
//                                contentDescription = stringResource(R.string.action_faq),
//                            )
//                        }
//                        IconButton(onClick = {
//                            intentTo(AboutActivity::class)
//                        }) {
//                            Icon(
//                                imageVector = Icons.TwoTone.Settings,
//                                contentDescription = stringResource(R.string.action_settings),
//                            )
//                        }
//                    }
//                )
            },
//            floatingActionButton = {
//                FloatingActionButton(onClick = {
//                    imageSelectLauncher.launch("image/*")
//                }) {
//                    Icon(
//                        imageVector = Icons.TwoTone.ImageSearch,
//                        contentDescription = null
//                    )
//                }
//            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(vertical = 8.dp)
            ) {
                val searchQuota by viewModel.searchQuota.collectAsState()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    item {
                        Card(
                            modifier = Modifier.padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BuildImage(listState.searchImageFile)
                                if (searchQuota != SearchQuota.EMPTY) {
                                    Text(
                                        modifier = Modifier
                                            .padding(8.dp),
                                        text = stringResource(R.string.hint_search_quota) + "${searchQuota.quotaUsed}/${searchQuota.quota}",
                                    )
                                }
                            }
                        }
                    }
                    items(listState.list) {
                        BuildResultItem(it, animeDialogState) {
                            viewModel.playVideo(it)
                        }
                    }
                }
            }
        }
        ShowProgressDialog(show = listState.loading, text = "TODO 加载中……")
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
                            StyledPlayerView(context).apply {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
}

@Composable
fun BuildResultItem(
    result: SearchAnimeResultItem,
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
            if (result.similarity < 0.9) {
                Text(
                    text = stringResource(R.string.hint_probably_mistake),
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
                        BuildText(result.aniList.title.native ?: "", FontWeight.Bold)
                        BuildText(result.aniList.title.english ?: "")
                        BuildText(result.aniList.title.romaji ?: "")
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
                            BuildText(stringResource(R.string.hint_time))
                            BuildText(stringResource(R.string.hint_episode))
                            BuildText(stringResource(R.string.hint_ani_list_id))
                            BuildText(stringResource(R.string.hint_mal_id))
                            BuildText(stringResource(R.string.hint_similarity), FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            BuildText("${(result.from.toLong() * 1000).formatTime()} ~ ${(result.to.toLong() * 1000).formatTime()}")
                            BuildText((result.episode ?: 0).toString())
                            BuildText("${result.aniList.id}")
                            BuildText("${result.aniList.idMal}")
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
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight
    )
}
