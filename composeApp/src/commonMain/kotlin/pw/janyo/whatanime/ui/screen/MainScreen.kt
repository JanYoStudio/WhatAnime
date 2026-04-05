package pw.janyo.whatanime.ui.screen

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Plagiarism
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.ui.components.BuildBottomSheet
import pw.janyo.whatanime.ui.components.BuildVideoDialog
import pw.janyo.whatanime.ui.components.SearchResultItem
import pw.janyo.whatanime.ui.components.ShowProgressDialog
import pw.janyo.whatanime.ui.components.rememberShowDialogState
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.navigation.RouteAbout
import pw.janyo.whatanime.ui.navigation.RouteHistory
import pw.janyo.whatanime.ui.navigation.RouteSettings
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.viewmodel.MainViewModel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.action_history
import whatanime.composeapp.generated.resources.action_open_source_license
import whatanime.composeapp.generated.resources.action_cancel
import whatanime.composeapp.generated.resources.action_ok
import whatanime.composeapp.generated.resources.action_settings
import whatanime.composeapp.generated.resources.action_start_search
import whatanime.composeapp.generated.resources.app_name
import whatanime.composeapp.generated.resources.drawer_api_quota_priority_action
import whatanime.composeapp.generated.resources.drawer_api_quota_priority_free
import whatanime.composeapp.generated.resources.drawer_api_quota_priority_hint
import whatanime.composeapp.generated.resources.drawer_api_quota_priority_label
import whatanime.composeapp.generated.resources.drawer_api_quota_refresh
import whatanime.composeapp.generated.resources.drawer_api_quota_set_api_key
import whatanime.composeapp.generated.resources.drawer_api_quota_title
import whatanime.composeapp.generated.resources.drawer_slogan
import whatanime.composeapp.generated.resources.hint_searching
import whatanime.composeapp.generated.resources.hint_select_to_search
import whatanime.composeapp.generated.resources.ic_whatanime
import whatanime.composeapp.generated.resources.settings_summary_api_key
import whatanime.composeapp.generated.resources.settings_title_api_key

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = LocalNavController.current!!
    val uriHandler = LocalUriHandler.current
    val context = LocalPlatformContext.current
    val vm = koinViewModel<MainViewModel>()

    val listState by vm.listState.collectAsState()

    val openBottomSheet = rememberSaveable { mutableStateOf(false) }
    var selectedItemForBottomSheet by remember { mutableStateOf<SearchAnimeResultItem?>(null) }

    val progressDialogState = rememberShowDialogState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var apiKeyDialogVisible by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(Configure.apiKey) }
    val pickerLauncher = rememberFilePickerLauncher(FileKitType.Image) { imageFile ->
        val image = imageFile ?: return@rememberFilePickerLauncher
        vm.searchImageFile(image)
    }

    fun doPickAndSearch() {
        pickerLauncher.launch()
    }

    BackHandler(drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
                // ── 顶部 Header：左侧文案 ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(Res.string.drawer_slogan),
                                modifier = Modifier.padding(start = 56.dp, end = 12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))

                // ── 导航列表 ──
                NavigationDrawerItem(
                    icon = { Icons(Icons.Filled.Plagiarism) },
                    label = { Text(stringResource(Res.string.action_history)) },
                    selected = false,
                    onClick = { navController.navigate(RouteHistory) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(vertical = 4.dp),
                )
                NavigationDrawerItem(
                    icon = { Icons(Icons.Outlined.Settings) },
                    label = { Text(stringResource(Res.string.action_settings)) },
                    selected = false,
                    onClick = { navController.navigate(RouteSettings) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(vertical = 4.dp),
                )
                NavigationDrawerItem(
                    icon = { Icons(Icons.Filled.Code) },
                    label = { Text(stringResource(Res.string.action_open_source_license)) },
                    selected = false,
                    onClick = { navController.navigate(RouteAbout) },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(vertical = 4.dp),
                )

                // ── API 额度卡片下沉到底部 ──
                Spacer(modifier = Modifier.weight(1f))
                val quota by vm.searchQuota.collectAsState()
                val displayQuotaTotal = if (quota.priority == 0 && quota.quota == 0) 100 else quota.quota
                val displayQuotaUsed = if (quota.priority == 0 && quota.quota == 0 && quota.quotaUsed == 0) 0 else quota.quotaUsed
                val quotaLoaded = displayQuotaTotal > 0
                val quotaProgress = if (quotaLoaded) {
                    (displayQuotaTotal - displayQuotaUsed).coerceAtLeast(0) / displayQuotaTotal.toFloat()
                } else {
                    0f
                }
                val priorityText = if (quota.priority == 0) {
                    stringResource(Res.string.drawer_api_quota_priority_free)
                } else {
                    quota.priority.toString()
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.drawer_api_quota_title),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = if (quotaLoaded) "$displayQuotaUsed / $displayQuotaTotal" else "-/-",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { quotaProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.drawer_api_quota_priority_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = priorityText,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.drawer_api_quota_priority_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { vm.showQuota() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icons(Icons.Outlined.Refresh)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.drawer_api_quota_refresh))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                apiKeyInput = Configure.apiKey
                                apiKeyDialogVisible = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.drawer_api_quota_set_api_key))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { uriHandler.openUri("https://github.com/sponsors/soruly") },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.drawer_api_quota_priority_action))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                                                selectedItemForBottomSheet = it
                                                openBottomSheet.value = true
                                            },
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
                                ExtendedFloatingActionButton(
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
    BuildBottomSheet(
        uriHandler = uriHandler,
        context = context,
        openBottomSheet = openBottomSheet,
        item = selectedItemForBottomSheet,
        onPlayVideo = {
            vm.playVideo(it)
        }
    )

    BuildVideoDialog()

    if (apiKeyDialogVisible) {
        AlertDialog(
            onDismissRequest = { apiKeyDialogVisible = false },
            title = { Text(stringResource(Res.string.settings_title_api_key)) },
            text = {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    label = { Text(stringResource(Res.string.settings_title_api_key)) },
                    supportingText = { Text(stringResource(Res.string.settings_summary_api_key)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Configure.apiKey = apiKeyInput
                        apiKeyDialogVisible = false
                        vm.showQuota()
                    }
                ) {
                    Text(stringResource(Res.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { apiKeyDialogVisible = false }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        )
    }

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
private fun BuildImage(searchImageFile: PlatformFile?) {
    var data: Any = Res.getUri("drawable/janyo_studio.png")
    searchImageFile?.let {
        data = searchImageFile
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
