package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.viewModel.TestViewModel

class ReceiveShareActivity : BaseComposeActivity<TestViewModel>() {
    override val viewModel: TestViewModel by viewModel()

    @Composable
    override fun BuildContent() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.exceptionData.observe(this, {
            if (it != null) {
                doNext()
            }
        })
        viewModel.completeTest.observe(this, {
            if (it) {
                doNext()
            }
        })
        viewModel.doTest()
    }

    private fun doNext() {
        if (intent != null &&
            intent.action == Intent.ACTION_SEND &&
            intent.type != null &&
            intent.type!!.startsWith("image/")
        ) {
            MainActivity.receiveShare(
                this,
                intent.getParcelableExtra(Intent.EXTRA_STREAM)!!,
                intent.type!!
            )
        } else {
            getString(R.string.hint_not_share).toast()
        }
        finish()
    }
}