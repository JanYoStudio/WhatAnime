package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pw.janyo.whatanime.config.setSecret

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application.setSecret("0d392422-670e-488b-b62b-b33cb2c15c3c")
        doNext()
    }

    private fun doNext() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}