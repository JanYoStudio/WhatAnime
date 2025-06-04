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
import androidx.compose.ui.Modifier
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pw.janyo.whatanime.model.DebugHttpInfo
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.copyToClipboard
import pw.janyo.whatanime.viewmodel.SettingsViewModel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.hint_copy_http_resource
import whatanime.composeapp.generated.resources.title_activity_settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun SettingsScreen() {
    val navController = LocalNavController.current!!
    val context = LocalPlatformContext.current
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
                        copyToClipboard(context, item.response)
                        showToast(getString(Res.string.hint_copy_http_resource))
                        state.value = null
                    }
                }
            ) {
                Text("拷贝")
            }
        },
        dismissButton = {
            TextButton(onClick = { state.value = null }) {
                Text("取消")
            }
        }
    )
}