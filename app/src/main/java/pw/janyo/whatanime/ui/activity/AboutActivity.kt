package pw.janyo.whatanime.ui.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import pw.janyo.whatanime.R
import pw.janyo.whatanime.appName
import pw.janyo.whatanime.appVersionName
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.ui.theme.Icons

class AboutActivity : BaseComposeActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = appName) },
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
            LibrariesContainer(
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

    @Composable
    fun AppInfo() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            Text(text = appName, color = MaterialTheme.colorScheme.onSurface)
            Text(text = appVersionName, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}