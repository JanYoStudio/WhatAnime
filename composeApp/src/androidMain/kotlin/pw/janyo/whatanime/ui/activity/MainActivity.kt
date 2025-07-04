package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import chaintech.videoplayer.util.PlaybackPreference
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import pw.janyo.whatanime.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        PlaybackPreference.initialize(this)
        setContent {
            App()
        }
    }
}