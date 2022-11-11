package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity

class ReceiveShareActivity : BaseComposeActivity() {
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
            @Suppress("DEPRECATION")
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                intent?.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            val type = contentResolver.getType(intent.data!!)
            intentTo(MainActivity::class, MainActivity.receiveShare(uri!!, type!!))
        } else {
            R.string.hint_not_share.toast()
        }
        finish()
    }
}