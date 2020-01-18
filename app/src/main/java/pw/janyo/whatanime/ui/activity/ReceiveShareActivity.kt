package pw.janyo.whatanime.ui.activity

import android.content.Intent
import androidx.databinding.ViewDataBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.APP
import pw.janyo.whatanime.viewModel.TestViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.toast

class ReceiveShareActivity : WABaseActivity<ViewDataBinding>(null) {
	private val testViewModel: TestViewModel by viewModel()

	override fun initData() {
		super.initData()
		testViewModel.connectServer.observe(this, object : PackageDataObserver<Boolean> {
			override fun content(data: Boolean?) {
				super.content(data)
				(application as APP).connectServer = data ?: false
				doNext()
			}

			override fun error(data: Boolean?, e: Throwable?) {
				super.error(data, e)
				if (e !is ResourceException) {
					Logs.wtf("error: ", e)
				}
				doNext()
			}
		})
	}

	override fun requestData() {
		super.requestData()
		testViewModel.doTest()
	}

	private fun doNext() {
		if (intent != null && intent.action == Intent.ACTION_SEND && intent.type != null && intent.type!!.startsWith("image/")) {
			MainActivity.receiveShare(this, intent.getParcelableExtra(Intent.EXTRA_STREAM)!!, intent.type!!)
		} else {
			getString(R.string.hint_not_share).toast(this)
		}
		finish()
	}
}