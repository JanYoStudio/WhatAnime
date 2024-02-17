package pw.janyo.whatanime.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import org.koin.core.component.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.appName
import pw.janyo.whatanime.appVersionName
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.constant.Constant
import pw.janyo.whatanime.constant.StringConstant.resString
import pw.janyo.whatanime.model.entity.NightMode
import pw.janyo.whatanime.publicDeviceId
import pw.janyo.whatanime.toCustomTabs
import pw.janyo.whatanime.ui.preference.CheckboxSetting
import pw.janyo.whatanime.ui.preference.ListSetting
import pw.janyo.whatanime.ui.preference.SettingsGroup
import pw.janyo.whatanime.ui.preference.SettingsMenuLink
import pw.janyo.whatanime.ui.preference.TextSettings
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.ui.theme.WaIcons
import pw.janyo.whatanime.viewModel.SettingsViewModel

class SettingsActivity : BaseComposeActivity() {
    private val viewModel: SettingsViewModel by viewModels()
    private val clipboardManager: ClipboardManager by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val hideSex by viewModel.hideSex.collectAsState()
        val nightMode by viewModel.nightMode.collectAsState()
        val showChineseTitle by viewModel.showChineseTitle.collectAsState()
        val allowSendCrashReport by viewModel.allowSendCrashReport.collectAsState()
        val searchQuota by viewModel.searchQuota.collectAsState()
        val customApiKey by viewModel.customApiKey.collectAsState()

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
                        Text(text = stringResource(id = R.string.settings_group_application))
                    },
                    content = {
                        CheckboxSetting(
                            title = stringResource(id = R.string.settings_title_hide_sex),
                            subtitle = stringResource(id = R.string.settings_summary_hide_sex),
                            checked = hideSex,
                            onCheckedChange = { newValue ->
                                viewModel.setHideSex(newValue)
                            }
                        )
                        ListSetting(
                            title = stringResource(id = R.string.settings_title_night_mode),
                            subtitle = stringResource(id = nightMode.title),
                            defaultValue = nightMode,
                            values = NightMode.selectList(),
                            valueToText = { AnnotatedString(it.title.resString()) },
                            onValueChange = {
                                viewModel.setNightMode(it)
                            }
                        )
                        CheckboxSetting(
                            title = stringResource(id = R.string.settings_title_show_chinese_title),
                            subtitle = stringResource(id = R.string.settings_summary_show_chinese_title),
                            checked = showChineseTitle,
                            onCheckedChange = { newValue ->
                                viewModel.setShowChineseTitle(newValue)
                            }
                        )
                        TextSettings(
                            title = stringResource(id = R.string.settings_title_api_key),
                            subtitle = stringResource(id = R.string.settings_summary_api_key),
                            defaultValue = customApiKey,
                            onValueChange = {
                                viewModel.setCustomApiKey(it)
                            }
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.action_donate),
                            subtitle = Constant.donateUrl,
                            onClick = {
                                toCustomTabs(Constant.donateUrl)
                            }
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_quota_used),
                            subtitle = stringResource(
                                id = R.string.settings_summary_quota_used,
                                searchQuota.quotaUsed
                            ),
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_quota_total),
                            subtitle = stringResource(
                                id = R.string.settings_summary_quota_total,
                                searchQuota.quota
                            ),
                        )
                    })
                SettingsGroup(
                    title = {
                        Text(text = stringResource(id = R.string.settings_group_more))
                    },
                    content = {
                        CheckboxSetting(
                            icon = { Icons(Icons.Outlined.BugReport) },
                            title = stringResource(id = R.string.settings_title_send_crash_report),
                            subtitle = stringResource(id = R.string.settings_summary_send_crash_report),
                            checked = allowSendCrashReport,
                            onCheckedChange = { newValue ->
                                viewModel.setAllowSendCrashReport(newValue)
                            }
                        )
                        SettingsMenuLink(
                            title = "",
                            subtitle = stringResource(id = R.string.settings_summary_app_center),
                        )
                    })
                SettingsGroup(
                    title = {
                        Text(text = stringResource(id = R.string.settings_group_about))
                    },
                    content = {
                        SettingsMenuLink(
                            icon = { Icons(WaIcons.Settings.github) },
                            title = stringResource(id = R.string.settings_title_about_github),
                            subtitle = stringResource(id = R.string.settings_summary_about_github),
                            onClick = {
                                toCustomTabs(R.string.settings_link_about_github.resString())
                            }
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_about_license),
                            subtitle = stringResource(id = R.string.settings_summary_about_license),
                            onClick = {
                                toCustomTabs(R.string.settings_link_about_license.resString())
                            }
                        )
                        SettingsMenuLink(
                            icon = { Icons(WaIcons.Settings.googlePlay) },
                            title = stringResource(id = R.string.settings_title_about_google_play),
                            subtitle = stringResource(id = R.string.settings_summary_about_google_play),
                            onClick = {
                                toCustomTabs(R.string.settings_link_about_google_play.resString())
                            }
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_about_janyo_license),
                            subtitle = stringResource(id = R.string.settings_summary_about_janyo_license),
                            onClick = {
                                toCustomTabs(R.string.settings_link_about_janyo_license.resString())
                            }
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_about_version),
                            subtitle = appVersionName,
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_about_device_id),
                            subtitle = publicDeviceId,
                            onClick = {
                                val clipData = ClipData.newPlainText(appName, publicDeviceId)
                                clipboardManager.setPrimaryClip(clipData)
                                R.string.hint_copy_device_id.toast()
                            }
                        )
                    })
                SettingsGroup(
                    title = {
                        Text(text = stringResource(id = R.string.settings_group_about_what_anime))
                    },
                    content = {
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_developer_what_anime),
                            subtitle = stringResource(id = R.string.settings_summary_developer_what_anime),
                            onClick = {
                                toCustomTabs(R.string.settings_link_developer_what_anime.resString())
                            }
                        )
                        SettingsMenuLink(
                            title = stringResource(id = R.string.settings_title_what_anime),
                            subtitle = stringResource(id = R.string.settings_summary_what_anime),
                            onClick = {
                                toCustomTabs(R.string.settings_link_what_anime.resString())
                            }
                        )
                    })
            }
        }

        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.isNotBlank()) {
            LaunchedEffect("errorMessage") {
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }
}