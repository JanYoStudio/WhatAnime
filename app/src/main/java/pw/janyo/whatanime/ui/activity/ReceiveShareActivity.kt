package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pw.janyo.whatanime.R
import vip.mystery0.tools.toast

class ReceiveShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doNext()
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