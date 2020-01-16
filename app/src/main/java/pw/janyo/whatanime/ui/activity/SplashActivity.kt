package pw.janyo.whatanime.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.ViewDataBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.APP
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.viewModel.TestViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.toastLong

class SplashActivity : WABaseActivity<ViewDataBinding>(null) {
	private val testViewModel: TestViewModel by viewModel()

	override fun initView() {
		super.initView()
		when (Configure.nightMode) {
			0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
			1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
			2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
			3 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
		}
	}

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
				if (e is ResourceException) {
					//网络异常，打印toast
					e.toastLong(this@SplashActivity)
				} else {
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
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}
}
