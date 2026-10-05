package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import pw.janyo.whatanime.ui.components.AppInfo
import pw.janyo.whatanime.ui.components.WorkspaceMessage
import pw.janyo.whatanime.ui.navigation.LocalNavController
import whatanime.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val nav = LocalNavController.current!!
    var libraries by remember { mutableStateOf<Libs?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(attempt) {
        failed = false
        try {
            libraries = withContext(Dispatchers.Default) { Libs.Builder().withJson(Res.readBytes("files/aboutlibraries.json").decodeToString()).build() }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) { failed = true }
    }
    Scaffold(topBar = { TopAppBar(
        title = { Text(stringResource(Res.string.action_open_source_license)) },
        navigationIcon = { IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.ui_back)) } },
    ) }) { padding ->
        when {
            failed -> Box(Modifier.padding(padding)) { WorkspaceMessage(stringResource(Res.string.hint_unknown_error), stringResource(Res.string.ui_retry)) { attempt++ } }
            libraries == null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else -> LibrariesContainer(libraries, Modifier.fillMaxSize(), header = { item { AppInfo() } }, contentPadding = padding)
        }
    }
}
