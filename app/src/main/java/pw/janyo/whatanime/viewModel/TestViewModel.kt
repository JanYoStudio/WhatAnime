package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import com.orhanobut.logger.Logger
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import pw.janyo.whatanime.api.IpApi
import pw.janyo.whatanime.api.ServerApi
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.config.*
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.model.request.TestRequest
import vip.mystery0.tools.utils.isConnectInternet
import java.util.*

class TestViewModel : ComposeViewModel(), KoinComponent {
    private val serverVipApi: ServerApi by inject(named("cloudVipApi"))
    private val serverAppApi: ServerApi by inject(named("cloudAppApi"))
    private val ipApi: IpApi by inject()

    val completeTest = MutableLiveData<String>()

    fun doTest() {
        launch {
            //判断是否联网
            if (!isConnectInternet()) {
                errorMessageState(StringConstant.hint_no_network)
                completeTest.postValue("")
                return@launch
            }
            //先尝试通过ip地址来判断是否是国内用户
            val geoResponse = withTimeoutOrNull(1000L) {
                ipApi.getGeoIp()
            }
            inChina = if (geoResponse == null) {
                Logger.w("get geo ip timeout")
                //超时了，大概率是国内的环境
                true
            } else {
                //查询到了ip地址信息，判断是否是国内
                geoResponse.location.country_code.lowercase(Locale.getDefault()) == "CN"
            }
            val response = withTimeoutOrNull(2000L) {
                if (inChina!!) {
                    serverVipApi.testStatistics(TestRequest())
                } else {
                    serverAppApi.testStatistics(TestRequest())
                }
            }
            connectServer = response != null
            if (response != null && response.useCloudCompress == null) {
                //服务端没有告知是否使用云端压缩
                //通过ip地址检测地区
                response.useCloudCompress = inChina
            }
            if (response == null) {
                inBlackList = false
                useServerCompress = false
            } else {
                inBlackList = response.inBlackList
                useServerCompress = response.useCloudCompress!!
            }
            if (Configure.requestType != 0) {
                //使用应用的配置项
                useServerCompress = Configure.requestType == 1
            }
            completeTest.postValue(response?.appCenterSecret ?: Configure.lastAppCenterSecret)
        }
    }
}