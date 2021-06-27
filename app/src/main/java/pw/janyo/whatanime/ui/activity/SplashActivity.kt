package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.config.setSecret
import pw.janyo.whatanime.viewModel.TestViewModel

class SplashActivity : AppCompatActivity() {
    private val viewModel: TestViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.exceptionData.observe(this, {
            if (it != null) {
                doNext()
            }
        })
        viewModel.completeTest.observe(this, {
            if (it.isNotBlank()) {
                application.setSecret(it)
            }
            doNext()
        })
        viewModel.doTest()
    }

    private fun doNext() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}