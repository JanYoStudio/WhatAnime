package pw.janyo.whatanime.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import vip.mystery0.tools.toast

class ReceiveShareActivity : WABaseActivity<ViewDataBinding>(null) {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (intent != null && intent.action == Intent.ACTION_SEND && intent.type != null && intent.type!!.startsWith("image/")) {
			MainActivity.receiveShare(this, intent.getParcelableExtra(Intent.EXTRA_STREAM)!!)
		} else {
			getString(R.string.hint_not_share).toast(this)
		}
	}
}