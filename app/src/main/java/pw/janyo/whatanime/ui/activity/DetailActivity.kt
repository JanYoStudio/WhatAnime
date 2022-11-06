package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.twotone.Preview
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.toCustomTabs
import pw.janyo.whatanime.utils.firstNotBlank
import pw.janyo.whatanime.viewModel.DetailViewModel
import java.io.File

class DetailActivity : BaseComposeActivity() {
    companion object {
        private const val TAG = "DetailActivity"
        private const val INTENT_HISTORY_ID = "INTENT_HISTORY_ID"
        private const val INTENT_CACHE_PATH = "INTENT_CACHE_PATH"
        private const val INTENT_TITLE = "INTENT_TITLE"

        fun showDetail(history: AnimationHistory): Intent.() -> Unit {
            return {
                putExtra(INTENT_HISTORY_ID, history.id)
                putExtra(INTENT_CACHE_PATH, history.cachePath)
                putExtra(INTENT_TITLE, history.title)
            }
        }
    }

    private val viewModel: DetailViewModel by viewModels()

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
        //查看历史记录
        val historyId = intent.getIntExtra(INTENT_HISTORY_ID, -1)
        val cacheFile = File(intent.getStringExtra(INTENT_CACHE_PATH)!!)
        //设置标题
        title = intent.getStringExtra(INTENT_TITLE)
        viewModel.loadHistoryDetail(historyId,cacheFile)
    }

    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val listState by viewModel.listState.collectAsState()
        val showFloatDialog by viewModel.showFloatDialog.collectAsState()

        val animeDialogState = remember { mutableStateOf<SearchAnimeResultItem?>(null) }

        val scaffoldState = rememberScaffoldState()
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
                                imageVector = Icons.TwoTone.Preview,
                                contentDescription = "",
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(listState.list) { item: SearchAnimeResultItem ->
                        BuildResultItem(item, animeDialogState) {
                            viewModel.playVideo(item)
                        }
                    }
                }

                Crossfade(
                    targetState = showFloatDialog,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
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
                                BuildImage(listState.searchImageFile)
                            }
                        }
                    }
                }
            }
        }
        BuildAlertDialog(animeDialogState)
        BuildVideoDialog()

        if (listState.errorMessage.isNotBlank()) {
            LaunchedEffect("errorMessage") {
                scaffoldState.snackbarHostState.showSnackbar(listState.errorMessage)
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
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(320.dp)
                .height(180.dp),
        )
    }
}