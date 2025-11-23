package pw.janyo.whatanime.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import org.jetbrains.compose.resources.stringResource
import pw.janyo.whatanime.ui.components.AppInfo
import pw.janyo.whatanime.ui.navigation.LocalNavController
import pw.janyo.whatanime.ui.theme.Icons
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.app_name

@Composable
fun AboutScreen() {
    val navController = LocalNavController.current!!
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.app_name)) },
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
        val libraries by produceLibraries {
            Res.readBytes("files/aboutlibraries.json").decodeToString()
        }
        LibrariesContainer(
            libraries,
            Modifier.fillMaxSize(),
            header = {
                item {
                    AppInfo()
                }
            },
            contentPadding = innerPadding,
        )
    }
}