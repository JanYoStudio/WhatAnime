package pw.janyo.whatanime.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.ViewDataBinding
import com.orhanobut.logger.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.config.connectServer
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.config.useServerCompress
import pw.janyo.whatanime.model.response.StatisticsResponse
import pw.janyo.whatanime.viewModel.TestViewModel
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.tools.ResourceException

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
        testViewModel.connectServer.observe(this, object : PackageDataObserver<StatisticsResponse> {
            override fun content(data: StatisticsResponse?) {
                super.content(data)
                connectServer = data != null
                data?.let {
                    inBlackList = it.inBlackList
                    useServerCompress = it.useCloudCompress!!
                }
                if (Configure.requestType != 0) {
                    //使用应用的配置项
                    useServerCompress = Configure.requestType == 1
                }
                doNext()
            }

            override fun error(data: StatisticsResponse?, e: Throwable?) {
                super.error(data, e)
                if (e is ResourceException) {
                    //网络异常，打印toast
                    e.toastLong()
                } else {
                    Logger.wtf("error: ", e)
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
