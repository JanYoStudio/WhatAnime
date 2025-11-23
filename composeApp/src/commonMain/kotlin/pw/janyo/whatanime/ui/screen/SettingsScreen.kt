package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.Constant
import pw.janyo.whatanime.base.appVersionName
import pw.janyo.whatanime.base.publicDeviceId
import pw.janyo.whatanime.model.DebugHttpInfo
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.preference.CheckboxSetting
import pw.janyo.whatanime.ui.preference.ListSetting
import pw.janyo.whatanime.ui.preference.SettingsGroup
import pw.janyo.whatanime.ui.preference.SettingsMenuLink
import pw.janyo.whatanime.ui.preference.TextSettings
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.ui.theme.WaIcons
import pw.janyo.whatanime.ui.theme.showNightModeSelectList
import pw.janyo.whatanime.utils.copyToClipboard
import pw.janyo.whatanime.utils.copyToClipboardThenToast
import pw.janyo.whatanime.viewmodel.SettingsViewModel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.action_cancel
import whatanime.composeapp.generated.resources.action_copy
import whatanime.composeapp.generated.resources.action_donate
import whatanime.composeapp.generated.resources.hint_copy_device_id
import whatanime.composeapp.generated.resources.settings_group_about
import whatanime.composeapp.generated.resources.settings_group_about_what_anime
import whatanime.composeapp.generated.resources.settings_group_application
import whatanime.composeapp.generated.resources.settings_link_about_github
import whatanime.composeapp.generated.resources.settings_link_about_google_play
import whatanime.composeapp.generated.resources.settings_link_about_janyo_license
import whatanime.composeapp.generated.resources.settings_link_about_license
import whatanime.composeapp.generated.resources.settings_link_developer_what_anime
import whatanime.composeapp.generated.resources.settings_link_what_anime
import whatanime.composeapp.generated.resources.settings_summary_about_github
import whatanime.composeapp.generated.resources.settings_summary_about_google_play
import whatanime.composeapp.generated.resources.settings_summary_about_janyo_license
import whatanime.composeapp.generated.resources.settings_summary_about_license
import whatanime.composeapp.generated.resources.settings_summary_api_key
import whatanime.composeapp.generated.resources.settings_summary_debug_mode
import whatanime.composeapp.generated.resources.settings_summary_developer_what_anime
import whatanime.composeapp.generated.resources.settings_summary_hide_sex
import whatanime.composeapp.generated.resources.settings_summary_prefer_webp
import whatanime.composeapp.generated.resources.settings_summary_quota_total
import whatanime.composeapp.generated.resources.settings_summary_quota_used
import whatanime.composeapp.generated.resources.settings_summary_what_anime
import whatanime.composeapp.generated.resources.settings_title_about_device_id
import whatanime.composeapp.generated.resources.settings_title_about_github
import whatanime.composeapp.generated.resources.settings_title_about_google_play
import whatanime.composeapp.generated.resources.settings_title_about_janyo_license
import whatanime.composeapp.generated.resources.settings_title_about_license
import whatanime.composeapp.generated.resources.settings_title_about_version
import whatanime.composeapp.generated.resources.settings_title_api_key
import whatanime.composeapp.generated.resources.settings_title_debug_mode
import whatanime.composeapp.generated.resources.settings_title_developer_what_anime
import whatanime.composeapp.generated.resources.settings_title_hide_sex
import whatanime.composeapp.generated.resources.settings_title_night_mode
import whatanime.composeapp.generated.resources.settings_title_prefer_webp
import whatanime.composeapp.generated.resources.settings_title_quota_total
import whatanime.composeapp.generated.resources.settings_title_quota_used
import whatanime.composeapp.generated.resources.settings_title_recent_http_responses
import whatanime.composeapp.generated.resources.settings_title_what_anime
import whatanime.composeapp.generated.resources.title_activity_settings

@Composable
fun SettingsScreen(){

    val navController = LocalNavController.current!!
    val context = LocalPlatformContext.current
    val uriHandler = LocalUriHandler.current
    val vm = koinViewModel<SettingsViewModel>()
    val hideSex by vm.hideSex.collectAsState()
    val preferWebp by vm.preferWebp.collectAsState()
    val nightMode by vm.nightMode.collectAsState()
    val searchQuota by vm.searchQuota.collectAsState()
    val customApiKey by vm.customApiKey.collectAsState()
    val debugMode by vm.debugMode.collectAsState()
    val httpResponses by vm.httpResponsesFlow.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val state = remember { mutableStateOf<DebugHttpInfo?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        vm.init()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(Res.string.title_activity_settings)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsGroup(
                title = {
                    Text(text = stringResource(Res.string.settings_group_application))
                },
                content = {
                    CheckboxSetting(
                        title = stringResource(Res.string.settings_title_hide_sex),
                        subtitle = stringResource(Res.string.settings_summary_hide_sex),
                        checked = hideSex,
                        onCheckedChange = { newValue ->
                            vm.setHideSex(newValue)
                        }
                    )
                    CheckboxSetting(
                        title = stringResource(Res.string.settings_title_prefer_webp),
                        subtitle = stringResource(Res.string.settings_summary_prefer_webp),
                        checked = preferWebp,
                        onCheckedChange = { newValue ->
                            vm.setPreferWebp(newValue)
                        }
                    )
                    val defaultNightMode = stringResource(nightMode.title)
                    val originShowNightModeSelectList = showNightModeSelectList()
                    val showNightModeSelectList =
                        originShowNightModeSelectList.map { stringResource(it.title) }
                    ListSetting(
                        title = stringResource(Res.string.settings_title_night_mode),
                        subtitle = stringResource(nightMode.title),
                        defaultValue = defaultNightMode,
                        values = showNightModeSelectList,
                        onValueChange = {
                            vm.setNightMode(
                                originShowNightModeSelectList[showNightModeSelectList.indexOf(
                                    it
                                )]
                            )
                        },
                    )
                    TextSettings(
                        title = stringResource(Res.string.settings_title_api_key),
                        subtitle = stringResource(Res.string.settings_summary_api_key),
                        defaultValue = customApiKey,
                        onValueChange = {
                            vm.setCustomApiKey(it)
                        }
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.action_donate),
                        subtitle = Constant.DONATE_URL,
                        onClick = {
                            uriHandler.openUri(Constant.DONATE_URL)
                        }
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_quota_used),
                        subtitle = stringResource(
                            Res.string.settings_summary_quota_used,
                            searchQuota.quotaUsed
                        ),
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_quota_total),
                        subtitle = stringResource(
                            Res.string.settings_summary_quota_total,
                            searchQuota.quota
                        ),
                    )
                })
            SettingsGroup(
                title = {
                    Text(text = stringResource(Res.string.settings_group_about))
                },
                content = {
                    SettingsMenuLink(
                        icon = { Icons(WaIcons.Settings.github) },
                        title = stringResource(Res.string.settings_title_about_github),
                        subtitle = stringResource(Res.string.settings_summary_about_github),
                        onClick = {
                            scope.launch {
                                uriHandler.openUri(getString(Res.string.settings_link_about_github))
                            }
                        }
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_about_license),
                        subtitle = stringResource(Res.string.settings_summary_about_license),
                        onClick = {
                            scope.launch {
                                uriHandler.openUri(getString(Res.string.settings_link_about_license))
                            }
                        }
                    )
                    SettingsMenuLink(
                        icon = { Icons(WaIcons.Settings.googlePlay) },
                        title = stringResource(Res.string.settings_title_about_google_play),
                        subtitle = stringResource(Res.string.settings_summary_about_google_play),
                        onClick = {
                            scope.launch {
                                uriHandler.openUri(getString(Res.string.settings_link_about_google_play))
                            }
                        }
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_about_janyo_license),
                        subtitle = stringResource(Res.string.settings_summary_about_janyo_license),
                        onClick = {
                            scope.launch {
                                uriHandler.openUri(getString(Res.string.settings_link_about_janyo_license))
                            }
                        }
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_about_version),
                        subtitle = appVersionName(),
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_about_device_id),
                        subtitle = publicDeviceId(),
                        onClick = {
                            scope.launch {
                                copyToClipboard(context, publicDeviceId())
                                showToast(getString(Res.string.hint_copy_device_id))
                            }
                        }
                    )
                })
            SettingsGroup(
                title = {
                    Text(text = stringResource(Res.string.settings_group_about_what_anime))
                },
                content = {
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_developer_what_anime),
                        subtitle = stringResource(Res.string.settings_summary_developer_what_anime),
                        onClick = {
                            scope.launch {
                                uriHandler.openUri(getString(Res.string.settings_link_developer_what_anime))
                            }
                        }
                    )
                    SettingsMenuLink(
                        title = stringResource(Res.string.settings_title_what_anime),
                        subtitle = stringResource(Res.string.settings_summary_what_anime),
                        onClick = {
                            scope.launch {
                                uriHandler.openUri(getString(Res.string.settings_link_what_anime))
                            }
                        }
                    )
                    CheckboxSetting(
                        title = stringResource(Res.string.settings_title_debug_mode),
                        subtitle = stringResource(Res.string.settings_summary_debug_mode),
                        checked = debugMode,
                        onCheckedChange = { newValue ->
                            vm.setDebugMode(newValue)
                        }
                    )
                })
            if (debugMode) {
                SettingsGroup(
                    title = {
                        Text(text = stringResource(Res.string.settings_title_recent_http_responses))
                    },
                    content = {
                        httpResponses.forEach { httpInfo ->
                            SettingsMenuLink(
                                title = httpInfo.title,
                                subtitle = httpInfo.datetime,
                                onClick = {
                                    state.value = httpInfo
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    BuildAlertDialog(state)

    val errorMessage by vm.errorMessage.collectAsState()
    if (errorMessage.isNotBlank()) {
        LaunchedEffect("errorMessage") {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }
}

@Composable
private fun BuildAlertDialog(state: MutableState<DebugHttpInfo?>) {
    if (state.value == null) return
    val item = state.value!!
    val context = LocalPlatformContext.current
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { state.value = null },
        title = {
            Text(text = "HTTP RESPONSE")
        },
        text = {
            val scrollState = rememberScrollState()
            Text(
                text = item.response,
                modifier = Modifier
                    .verticalScroll(scrollState)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        copyToClipboardThenToast(context, item.response)
                        state.value = null
                    }
                }
            ) {
                Text(stringResource(Res.string.action_copy))
            }
        },
        dismissButton = {
            TextButton(onClick = { state.value = null }) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}