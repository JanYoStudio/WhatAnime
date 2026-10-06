package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.base.*
import pw.janyo.whatanime.model.DebugHttpInfo
import pw.janyo.whatanime.model.QuotaPhase
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.navigation.RouteAbout
import pw.janyo.whatanime.ui.preference.*
import pw.janyo.whatanime.ui.theme.showNightModeSelectList
import pw.janyo.whatanime.utils.copyToClipboardThenToast
import pw.janyo.whatanime.viewmodel.SettingsViewModel
import whatanime.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = koinViewModel()) {
    val nav = LocalNavController.current!!
    val context = LocalPlatformContext.current
    val cutBorders by vm.cutBorders.collectAsState()
    val hideSex by vm.hideSex.collectAsState()
    val preferWebp by vm.preferWebp.collectAsState()
    val nightMode by vm.nightMode.collectAsState()
    val debugMode by vm.debugMode.collectAsState()
    val responses by vm.httpResponsesFlow.collectAsState()
    val hasKey by vm.hasApiKey.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    var debugItem by remember { mutableStateOf<DebugHttpInfo?>(null) }
    var editingKey by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { vm.init() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(Res.string.title_activity_settings)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            QuotaCard(vm)
            SettingsGroup(title = { Text(stringResource(Res.string.ui_search_settings)) }, content = {
                CheckboxSetting(title = stringResource(Res.string.action_cut_border), subtitle = stringResource(Res.string.settings_summary_cut_border), checked = cutBorders, onCheckedChange = vm::setCutBorders)
                CheckboxSetting(title = stringResource(Res.string.settings_title_hide_sex), subtitle = stringResource(Res.string.settings_summary_hide_sex), checked = hideSex, onCheckedChange = vm::setHideSex)
                CheckboxSetting(title = stringResource(Res.string.settings_title_prefer_webp), subtitle = stringResource(Res.string.settings_summary_prefer_webp), checked = preferWebp, onCheckedChange = vm::setPreferWebp)
            })
            SettingsGroup(title = { Text(stringResource(Res.string.ui_appearance)) }, content = {
                val modes = showNightModeSelectList()
                ListSetting(
                    title = stringResource(Res.string.settings_title_night_mode), subtitle = stringResource(nightMode.title),
                    defaultValue = nightMode, values = modes,
                    valueToText = { AnnotatedString(stringResource(it.title)) },
                    onValueChange = vm::setNightMode,
                )
            })
            SettingsGroup(title = { Text(stringResource(Res.string.ui_service)) }, content = {
                SettingsMenuLink(
                    title = stringResource(Res.string.settings_title_api_key),
                    subtitle = stringResource(if (hasKey) Res.string.ui_key_set else Res.string.ui_key_unset),
                    onClick = { input = vm.customApiKey.value; keyVisible = false; editingKey = true; vm.acknowledgeError() },
                )
            })
            SettingsGroup(title = { Text(stringResource(Res.string.ui_debug)) }, content = {
                CheckboxSetting(title = stringResource(Res.string.settings_title_debug_mode), subtitle = stringResource(Res.string.settings_summary_debug_mode), checked = debugMode, onCheckedChange = vm::setDebugMode)
                if (debugMode) {
                    Text(stringResource(Res.string.settings_title_recent_http_responses), Modifier.padding(16.dp))
                    responses.forEach { response -> SettingsMenuLink(title = response.title, subtitle = response.datetime, onClick = { debugItem = response }) }
                }
            })
            SettingsGroup(title = { Text(stringResource(Res.string.settings_group_about)) }, content = {
                ExternalLink(stringResource(Res.string.settings_title_about_github), stringResource(Res.string.settings_summary_about_github), stringResource(Res.string.settings_link_about_github))
                ExternalLink(stringResource(Res.string.settings_title_about_license), stringResource(Res.string.settings_summary_about_license), stringResource(Res.string.settings_link_about_license))
                ExternalLink(stringResource(Res.string.settings_title_about_janyo_license), stringResource(Res.string.settings_summary_about_janyo_license), stringResource(Res.string.settings_link_about_janyo_license))
                ExternalLink(stringResource(getStoreTitle()), stringResource(getStoreUrl()), stringResource(getStoreUrl()))
                SettingsMenuLink(title = stringResource(Res.string.settings_title_about_version), subtitle = appVersionName())
                SettingsMenuLink(title = stringResource(Res.string.settings_title_about_device_id), subtitle = publicDeviceId(), onClick = { scope.launch { copyToClipboardThenToast(context, publicDeviceId()) } })
                SettingsMenuLink(title = stringResource(Res.string.action_open_source_license), onClick = { nav.navigate(RouteAbout) { launchSingleTop = true } })
            })
            SettingsGroup(title = { Text(stringResource(Res.string.settings_group_about_what_anime)) }, content = {
                ExternalLink(stringResource(Res.string.settings_title_developer_what_anime), stringResource(Res.string.settings_summary_developer_what_anime), stringResource(Res.string.settings_link_developer_what_anime))
                ExternalLink(stringResource(Res.string.settings_title_what_anime), stringResource(Res.string.settings_summary_what_anime), stringResource(Res.string.settings_link_what_anime))
                ExternalLink(stringResource(Res.string.drawer_api_quota_donate_sponsors), "soruly", "https://github.com/sponsors/soruly")
                ExternalLink(stringResource(Res.string.drawer_api_quota_donate_patreon), "soruly", "https://www.patreon.com/soruly")
            })
            Spacer(Modifier.height(16.dp))
        }
    }
    if (editingKey) {
        fun dismissKey() { if (!saving) { editingKey = false; input = ""; keyVisible = false } }
        AlertDialog(
            onDismissRequest = ::dismissKey,
            title = { Text(stringResource(Res.string.ui_configure_api_key)) },
            text = { Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.settings_summary_api_key))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.settings_title_api_key)) },
                    value = input, onValueChange = { input = it }, enabled = !saving, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(if (keyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, stringResource(if (keyVisible) Res.string.ui_hide_key else Res.string.ui_show_key))
                    } },
                )
                if (errorMessage.isNotBlank()) Text(errorMessage, color = MaterialTheme.colorScheme.error)
            } },
            confirmButton = { TextButton(enabled = !saving, onClick = {
                saving = true
                scope.launch {
                    try { if (vm.saveApiKey(input)) { editingKey = false; input = ""; keyVisible = false } }
                    finally { saving = false }
                }
            }) { Text(stringResource(Res.string.ui_save_refresh)) } },
            dismissButton = { TextButton(enabled = !saving, onClick = ::dismissKey) { Text(stringResource(Res.string.action_cancel)) } },
        )
    }
    debugItem?.let { item ->
        AlertDialog(
            onDismissRequest = { debugItem = null }, title = { Text(stringResource(Res.string.settings_title_recent_http_responses)) },
            text = { Text(item.response, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { scope.launch { copyToClipboardThenToast(context, item.response); debugItem = null } }) { Text(stringResource(Res.string.action_copy)) } },
            dismissButton = { TextButton(onClick = { debugItem = null }) { Text(stringResource(Res.string.ui_close)) } },
        )
    }
}

@Composable
private fun QuotaCard(vm: SettingsViewModel) {
    val state by vm.quotaState.collectAsState()
    ElevatedCard(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.ui_quota_title), style = MaterialTheme.typography.titleMedium)
            state.value?.let { quota ->
                if (state.stale) Text(stringResource(Res.string.ui_quota_stale))
                Text("${stringResource(Res.string.drawer_api_quota_priority_label)}: ${quota.priority}")
                Text(stringResource(Res.string.hint_quota_total, quota.quota))
                Text(stringResource(Res.string.hint_quota_used, quota.quotaUsed))
                state.remaining?.let { Text(stringResource(Res.string.ui_remaining, it)) }
                state.progress?.let { LinearProgressIndicator(progress = { it }, modifier = Modifier.fillMaxWidth()) }
                if (state.progress == null) Text(stringResource(Res.string.ui_quota_unknown))
                if (state.exhausted && !state.stale) Text(stringResource(Res.string.ui_quota_exhausted))
            }
            when (state.phase) {
                QuotaPhase.NotLoaded -> Text(stringResource(Res.string.ui_quota_unknown))
                QuotaPhase.Loading -> { LinearProgressIndicator(Modifier.fillMaxWidth()); Text(stringResource(Res.string.ui_loading)) }
                QuotaPhase.Error -> Text(stringResource(Res.string.ui_quota_failed), color = MaterialTheme.colorScheme.error)
                QuotaPhase.Ready -> Unit
            }
            OutlinedButton(onClick = { vm.refreshQuota() }, enabled = state.phase != QuotaPhase.Loading) { Text(stringResource(Res.string.drawer_api_quota_refresh)) }
        }
    }
}

@Composable
private fun ExternalLink(title: String, subtitle: String, url: String) {
    val uriHandler = LocalUriHandler.current
    SettingsMenuLink(title = title, subtitle = subtitle, onClick = { uriHandler.openUri(url) })
}
