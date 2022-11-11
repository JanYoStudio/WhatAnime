package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import androidx.compose.runtime.Composable
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.registerAppCenter

class StartActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerAppCenter(application)
    }

    @Composable
    override fun BuildContent() {
        goToMainScreen()
    }

    private fun goToMainScreen() {
        intentTo(MainActivity::class)
        finish()
    }
}