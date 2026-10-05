package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.model.DetailPhase
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.ui.components.*
import pw.janyo.whatanime.ui.navigation.*
import pw.janyo.whatanime.utils.isVideoExpired
import pw.janyo.whatanime.viewmodel.DetailViewModel
import whatanime.composeapp.generated.resources.*
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(historyId: Int, cachePath: String) {
    val vm = koinViewModel<DetailViewModel>()
    val state by vm.detailState.collectAsState()
    val notice by vm.notice.collectAsState()
    val navController = LocalNavController.current!!
    val listState = rememberLazyListState()
    val menuOpen = remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<SearchAnimeResultItem?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val expired by produceState(false, state.savedAt) {
        val savedAt = state.savedAt ?: return@produceState
        while (true) {
            value = isVideoExpired(savedAt, Clock.System.now().toEpochMilliseconds())
            if (value) break
            delay(1000)
        }
    }
    LaunchedEffect(historyId) {
        if (vm.detailState.value.historyId != historyId) vm.loadHistoryDetail(historyId)
    }
    LaunchedEffect(notice) {
        if (notice.isNotBlank()) { snackbar.showSnackbar(notice); vm.acknowledgeNotice() }
    }
    Scaffold(
        topBar = { TopAppBar(
            title = { Text(stringResource(Res.string.ui_detail_title)) },
            navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.ui_back)) } },
        ) }, snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        SearchWorkspace(
            image = state.image, results = state.results, loading = state.phase == DetailPhase.Loading,
            listState = listState, modifier = Modifier.padding(padding).fillMaxSize(),
            status = {
                if (expired) Text(stringResource(Res.string.video_play_hint_410), color = MaterialTheme.colorScheme.error)
                when (state.phase) {
                    DetailPhase.Loading -> Text(stringResource(Res.string.ui_loading))
                    DetailPhase.Ready -> Unit
                    DetailPhase.Empty -> WorkspaceMessage(stringResource(Res.string.hint_no_result))
                    DetailPhase.FilteredEmpty -> WorkspaceMessage(stringResource(Res.string.ui_filtered_empty), stringResource(Res.string.action_settings)) {
                        navController.selectTopLevel(TopLevelDestination.Settings)
                    }
                    DetailPhase.NotFound -> WorkspaceMessage(stringResource(Res.string.ui_history_missing), stringResource(Res.string.action_history)) { navController.popBackStack() }
                    DetailPhase.Error -> WorkspaceMessage(stringResource(Res.string.hint_unknown_error), stringResource(Res.string.ui_retry)) { vm.loadHistoryDetail(historyId) }
                    DetailPhase.Legacy -> WorkspaceMessage(stringResource(Res.string.hint_data_convert_no_detail_in_history))
                }
            },
            onPlay = vm::playVideo, onMenu = { selected = it; menuOpen.value = true },
        )
    }
    BuildBottomSheet(LocalUriHandler.current, LocalPlatformContext.current, menuOpen, selected, vm::playVideo)
}
