package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.LocalPlatformContext
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.ui.components.PlatformMediaPlayerView
import pw.janyo.whatanime.ui.components.PlaybackState
import pw.janyo.whatanime.ui.components.SearchResultItem
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.viewmodel.DetailViewModel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.action_cancel
import whatanime.composeapp.generated.resources.action_ok
import whatanime.composeapp.generated.resources.hint_click_to_show_anilist_info
import whatanime.composeapp.generated.resources.hint_show_animation_detail
import whatanime.composeapp.generated.resources.title_activity_history
import whatanime.composeapp.generated.resources.video_play_hint_410

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(historyId: Int, cachePath: String) {
    val navController = LocalNavController.current!!
    val vm = koinViewModel<DetailViewModel>()

    val listState by vm.listState.collectAsState()
    val playbackState by vm.playBackState.collectAsState()

    val animeDialogState = remember { mutableStateOf<SearchAnimeResultItem?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(Res.string.title_activity_history)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icons(Icons.AutoMirrored.Filled.ArrowBack)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            showToast(getString(Res.string.hint_click_to_show_anilist_info))
                        }
                    }) {
                        Icons(Icons.Outlined.TipsAndUpdates)
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(listState.list) { item: SearchAnimeResultItem ->
                SearchResultItem(
                    item,
                    onClick = {
                        animeDialogState.value = item
                    },
                    onClickImage = {
                        if (listState.tokenExpired) {
                            scope.launch {
                                showToast(getString(Res.string.video_play_hint_410))
                            }
                            return@SearchResultItem
                        }
                        vm.playVideo(item)
                    }
                )
            }
        }
    }
    BuildAlertDialog(animeDialogState)
    BuildVideoDialog(playbackState)

    LaunchedEffect(listState) {
        if (listState.errorMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(listState.errorMessage)
        }
    }
    LaunchedEffect(Unit) {
        val cacheFile = PlatformFile(cachePath)
        vm.loadHistoryDetail(historyId, cacheFile)
    }
}


@Composable
private fun BuildAlertDialog(animeDialogState: MutableState<SearchAnimeResultItem?>) {
    if (animeDialogState.value == null) return
    val item = animeDialogState.value!!
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = { animeDialogState.value = null },
        text = {
            Text(
                text = stringResource(
                    Res.string.hint_show_animation_detail,
                    item.aniList.title.native ?: item.fileName
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    uriHandler.openUri("https://anilist.co/anime/${item.aniList.id}")
                    animeDialogState.value = null
                }
            ) {
                Text(stringResource(Res.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { animeDialogState.value = null }) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@Composable
private fun BuildVideoDialog(playbackState: PlaybackState) {
    val vm = koinViewModel<DetailViewModel>()
    val controller = remember { vm.getPlatformController() }
    if (playbackState == PlaybackState.Stop || playbackState is PlaybackState.Error) {
        return
    }
    Dialog(onDismissRequest = { }, content = {
        Box(modifier = Modifier.padding(8.dp)) {
            PlatformMediaPlayerView(
                modifier = Modifier
                    .width(480.dp)
                    .height(270.dp),
                controller,
            )
            if (controller.isLoading()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    })
}