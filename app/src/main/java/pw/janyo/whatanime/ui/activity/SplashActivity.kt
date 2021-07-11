package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doNext()
    }

    private fun doNext() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}