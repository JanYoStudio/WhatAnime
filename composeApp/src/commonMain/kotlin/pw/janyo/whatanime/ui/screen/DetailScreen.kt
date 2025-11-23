package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.ui.components.BuildBottomSheet
import pw.janyo.whatanime.ui.components.BuildVideoDialog
import pw.janyo.whatanime.ui.components.SearchResultItem
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.viewmodel.DetailViewModel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.title_activity_history
import whatanime.composeapp.generated.resources.video_play_hint_410

@Composable
fun DetailScreen(historyId: Int, cachePath: String) {
    val navController = LocalNavController.current!!
    val vm = koinViewModel<DetailViewModel>()

    val listState by vm.listState.collectAsState()

    val openBottomSheet = rememberSaveable { mutableStateOf(false) }
    var selectedItemForBottomSheet by remember { mutableStateOf<SearchAnimeResultItem?>(null) }

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
                        selectedItemForBottomSheet = item
                        openBottomSheet.value = true
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

    if (selectedItemForBottomSheet != null) {
        BuildBottomSheet(
            openBottomSheet = openBottomSheet,
            item = selectedItemForBottomSheet!!,
            onPlayVideo = {
                vm.playVideo(it)
            }
        )
    }

    BuildVideoDialog()

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
