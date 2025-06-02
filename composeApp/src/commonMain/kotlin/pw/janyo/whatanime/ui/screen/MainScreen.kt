package pw.janyo.whatanime.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Plagiarism
import androidx.compose.material.icons.outlined.AppShortcut
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TipsAndUpdates
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.exists
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.Constant
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.ui.components.PlatformMediaPlayerView
import pw.janyo.whatanime.ui.components.PlaybackState
import pw.janyo.whatanime.ui.components.SearchResultItem
import pw.janyo.whatanime.ui.components.ShowProgressDialog
import pw.janyo.whatanime.ui.components.rememberShowDialogState
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.navigation.RouteAbout
import pw.janyo.whatanime.ui.navigation.RouteHistory
import pw.janyo.whatanime.ui.navigation.RouteSettings
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.toCustomTabs
import pw.janyo.whatanime.viewmodel.MainViewModel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.action_about_janyo
import whatanime.composeapp.generated.resources.action_about_whatanime
import whatanime.composeapp.generated.resources.action_cancel
import whatanime.composeapp.generated.resources.action_cut_border
import whatanime.composeapp.generated.resources.action_history
import whatanime.composeapp.generated.resources.action_ok
import whatanime.composeapp.generated.resources.action_settings
import whatanime.composeapp.generated.resources.action_start_search
import whatanime.composeapp.generated.resources.app_name
import whatanime.composeapp.generated.resources.hint_click_to_show_anilist_info
import whatanime.composeapp.generated.resources.hint_quota_total
import whatanime.composeapp.generated.resources.hint_quota_used
import whatanime.composeapp.generated.resources.hint_searching
import whatanime.composeapp.generated.resources.hint_select_file_not_exist
import whatanime.composeapp.generated.resources.hint_select_to_search
import whatanime.composeapp.generated.resources.hint_show_animation_detail
import whatanime.composeapp.generated.resources.ic_whatanime
import whatanime.composeapp.generated.resources.settings_group_about

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = LocalNavController.current!!
    val context = LocalPlatformContext.current
    val vm = koinViewModel<MainViewModel>()

    val listState by vm.listState.collectAsState()
    val cutBorders by vm.cutBorders.collectAsState()
    val playbackState by vm.playBackState.collectAsState()

    val animeDialogState = remember { mutableStateOf<SearchAnimeResultItem?>(null) }

    val progressDialogState = rememberShowDialogState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun doPickAndSearch() {
        scope.launch {
            val imageFile = FileKit.openFilePicker(type = FileKitType.Image) ?: return@launch
            if (!imageFile.exists()) {
                showToast(getString(Res.string.hint_select_file_not_exist))
            } else {
                vm.searchImageFile(imageFile)
            }
        }
    }

    BackHandler(drawerState.isOpen){
        scope.launch {
            drawerState.close()
        }
    }

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
                            painter = painterResource(Res.drawable.ic_whatanime),
                            contentDescription = "logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = stringResource(Res.string.app_name),
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
                    val quota by vm.searchQuota.collectAsState()
                    Text(
                        text = stringResource(Res.string.hint_quota_used, quota.quotaUsed),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(Res.string.hint_quota_total, quota.quota))
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                NavigationDrawerItem(
                    icon = { Icons(Icons.Filled.Plagiarism) },
                    label = { Text(stringResource(Res.string.action_history)) },
                    selected = false,
                    onClick = {
                        navController.navigate(RouteHistory)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icons(Icons.Outlined.Settings) },
                    label = { Text(stringResource(Res.string.action_settings)) },
                    selected = false,
                    onClick = {
                        navController.navigate(RouteSettings)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icons(Icons.Outlined.AppShortcut) },
                    label = { Text(stringResource(Res.string.action_cut_border)) },
                    badge = {
                        Switch(checked = cutBorders, onCheckedChange = {
                            vm.changeCutBorders()
                        })
                    },
                    selected = true,
                    onClick = {
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icons(Icons.Filled.AutoAwesome) },
                    label = { Text(stringResource(Res.string.settings_group_about)) },
                    selected = false,
                    onClick = {
                        navController.navigate(RouteAbout)
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
                            toCustomTabs(context, Constant.janYoStudioUrl)
                        }) {
                        Text(
                            text = stringResource(Res.string.action_about_janyo),
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
                            toCustomTabs(context, Constant.whatAnimeUrl)
                        }) {
                        Text(
                            text = stringResource(Res.string.action_about_whatanime),
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
                                    painter = painterResource(Res.drawable.ic_whatanime),
                                    contentDescription = "logo",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.app_name),
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
                            if (!listState.list.isEmpty()) {
                                IconButton(onClick = {
                                    scope.launch {
                                        showToast(getString(Res.string.hint_click_to_show_anilist_info))
                                    }
                                }) {
                                    Icons(Icons.Outlined.TipsAndUpdates)
                                }
                            }
                            IconButton(onClick = {
                                doPickAndSearch()
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
                                                doPickAndSearch()
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
                                            modifier = Modifier.fillMaxHeight()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                fontSize = 16.sp,
                                                text = stringResource(Res.string.hint_select_to_search),
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    items(listState.list) {
                                        SearchResultItem(
                                            it,
                                            onClick = {
                                                animeDialogState.value = it
                                            },
                                            onClickImage = {
                                                vm.playVideo(it)
                                            }
                                        )
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
                                val transition = rememberInfiniteTransition()
                                val translationY by transition.animateFloat(
                                    initialValue = 0F,
                                    targetValue = -24F,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(durationMillis = 750, easing = EaseInOut),
                                        repeatMode = RepeatMode.Reverse,
                                    )
                                )
                                ExtendedFloatingActionButton(
                                    modifier = Modifier.offset(y = translationY.dp),
                                    text = {
                                        Text(text = stringResource(Res.string.action_start_search))
                                    },
                                    icon = {
                                        Icons(Icons.Outlined.ImageSearch)
                                    },
                                    onClick = {
                                        doPickAndSearch()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    )
    ShowProgressDialog(
        state = progressDialogState,
        text = stringResource(Res.string.hint_searching)
    )
    BuildAlertDialog(animeDialogState)
    BuildVideoDialog(playbackState)

    LaunchedEffect(listState) {
        progressDialogState.show = listState.loading
        if (listState.errorMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(listState.errorMessage)
        }
    }
    LaunchedEffect(Unit) {
        vm.showQuota()
    }
}

@Composable
private fun BuildAlertDialog(animeDialogState: MutableState<SearchAnimeResultItem?>) {
    if (animeDialogState.value == null) return
    val item = animeDialogState.value!!
    val context = LocalPlatformContext.current
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
                    toCustomTabs(context, "https://anilist.co/anime/${item.aniList.id}")
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
    val vm = koinViewModel<MainViewModel>()
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

@Composable
private fun BuildImage(searchImageFile: PlatformFile?) {
    var data: Any = Res.getUri("drawable/janyo_studio.png")
    searchImageFile?.let {
        if (!it.exists()) {
            showToast(stringResource(Res.string.hint_select_file_not_exist))
        } else {
            data = it
        }
    }
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
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

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)