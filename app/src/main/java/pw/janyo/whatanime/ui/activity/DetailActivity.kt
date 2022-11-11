package pw.janyo.whatanime.ui.activity

import android.content.Intent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.toCustomTabs
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.firstNotBlank
import pw.janyo.whatanime.viewModel.DetailViewModel
import java.io.File

class DetailActivity : BaseComposeActivity() {
    companion object {
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
        viewModel.loadHistoryDetail(historyId, cacheFile)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val listState by viewModel.listState.collectAsState()
        val showChineseTitle by viewModel.showChineseTitle.collectAsState()

        val animeDialogState = remember { mutableStateOf<SearchAnimeResultItem?>(null) }

        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            Icons(Icons.Filled.ArrowBack)
                        }
                    },
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(listState.list) { item: SearchAnimeResultItem ->
                    BuildResultItem(item, showChineseTitle, animeDialogState) {
                        viewModel.playVideo(item)
                    }
                }
            }
        }
        BuildAlertDialog(animeDialogState)
        BuildVideoDialog()

        if (listState.errorMessage.isNotBlank()) {
            LaunchedEffect("errorMessage") {
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
}