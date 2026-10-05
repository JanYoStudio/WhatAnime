package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.model.SearchPhase
import pw.janyo.whatanime.ui.components.*
import pw.janyo.whatanime.ui.navigation.*
import pw.janyo.whatanime.viewmodel.MainViewModel
import whatanime.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = koinViewModel()) {
    val state by vm.searchState.collectAsState()
    val restorationNeedsSelection by vm.restorationNeedsSelection.collectAsState()
    val navController = LocalNavController.current!!
    val listState = rememberLazyListState()
    val snackbar = remember { SnackbarHostState() }
    val menuOpen = remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<SearchAnimeResultItem?>(null) }
    var fabHeight by remember { mutableIntStateOf(0) }
    val loading = state.phase == SearchPhase.Loading
    var pickerOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pickerFailure = stringResource(Res.string.hint_unknown_error)
    val picker = rememberFilePickerLauncher(FileKitType.Image) { file ->
        pickerOpen = false
        file?.let(vm::searchImageFile)
    }
    val busy = loading || pickerOpen
    val label = stringResource(if (state.phase == SearchPhase.Idle) Res.string.action_start_search else Res.string.ui_change_image)
    LaunchedEffect(state.requestId) {
        if (state.phase == SearchPhase.Loading) listState.scrollToItem(0)
    }
    LaunchedEffect(state.requestId, state.errorMessage) {
        if (!state.errorAcknowledged && state.errorMessage != null) {
            vm.acknowledgeError(state.requestId)
            snackbar.showSnackbar(state.errorMessage!!)
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.app_name)) }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        SearchWorkspace(
            image = state.image, results = state.results, loading = loading, listState = listState,
            modifier = Modifier.padding(padding).fillMaxSize(),
            bottomSpace = with(LocalDensity.current) { fabHeight.toDp() } + 32.dp,
            status = {
                when (state.phase) {
                    SearchPhase.Idle -> Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Outlined.ImageSearch, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(Res.string.hint_select_to_search), style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(Res.string.ui_image_limit))
                        if (restorationNeedsSelection) Text(stringResource(Res.string.ui_interrupted))
                    }
                    SearchPhase.Loading -> Text(stringResource(Res.string.hint_searching), Modifier.padding(vertical = 8.dp))
                    SearchPhase.Success -> Unit
                    SearchPhase.Empty -> WorkspaceMessage(stringResource(Res.string.hint_no_result))
                    SearchPhase.FilteredEmpty -> WorkspaceMessage(stringResource(Res.string.ui_filtered_empty), stringResource(Res.string.action_settings)) {
                        navController.selectTopLevel(TopLevelDestination.Settings)
                    }
                    SearchPhase.Error -> WorkspaceMessage(state.errorMessage ?: stringResource(Res.string.hint_search_error), stringResource(Res.string.ui_retry), vm::retry)
                }
            },
            onPlay = vm::playVideo,
            onMenu = { selected = it; menuOpen.value = true },
            overlay = {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!busy) {
                            pickerOpen = true
                            try { picker.launch() } catch (_: Exception) {
                                pickerOpen = false
                                scope.launch { snackbar.showSnackbar(pickerFailure) }
                            }
                        }
                    },
                    icon = { Icon(Icons.Outlined.ImageSearch, label) },
                    text = { Text(label) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp)
                        .onSizeChanged { fabHeight = it.height }.alpha(if (busy) 0.38f else 1f)
                        .semantics { if (busy) disabled() },
                )
            },
        )
    }
    BuildBottomSheet(LocalUriHandler.current, LocalPlatformContext.current, menuOpen, selected, vm::playVideo)
}

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
