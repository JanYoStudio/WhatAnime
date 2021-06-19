package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.viewModel.TestViewModel

class SplashActivity : BaseComposeActivity<TestViewModel>() {
    override val viewModel: TestViewModel by viewModel()

    @Composable
    override fun BuildContent() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.exceptionData.observe(this, {
            doNext()
        })
        viewModel.completeTest.observe(this, {
            doNext()
        })
        viewModel.doTest()
    }

    private fun doNext() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}