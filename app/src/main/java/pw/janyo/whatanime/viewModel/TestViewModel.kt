package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import pw.janyo.whatanime.R
import pw.janyo.whatanime.api.IpApi
import pw.janyo.whatanime.api.ServerApi
import pw.janyo.whatanime.config.inChina
import pw.janyo.whatanime.model.request.TestRequest
import pw.janyo.whatanime.model.response.StatisticsResponse
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.launch
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.utils.isConnectInternet
import java.util.*

class TestViewModel : ViewModel(), KoinComponent {
    private val serverVipApi: ServerApi by inject(named("cloudVipApi"))
    private val serverAppApi: ServerApi by inject(named("cloudAppApi"))
    private val ipApi: IpApi by inject()

    val connectServer = MediatorLiveData<PackageData<StatisticsResponse>>()

    fun doTest() {
        launch(connectServer) {
            //判断是否联网
            if (!isConnectInternet()) {
                throw ResourceException(R.string.hint_no_network)
            }
            //先尝试通过ip地址来判断是否是国内用户
            val geoResponse = withTimeoutOrNull(1000L) {
                ipApi.getGeoIp()
            }
            inChina = if (geoResponse == null) {
                Logs.w("get geo ip timeout")
                //超时了，大概率是国内的环境
                true
            } else {
                //查询到了ip地址信息，判断是否是国内
                geoResponse.location.country_code.toLowerCase(Locale.getDefault()) == "CN"
            }
            val response = withTimeoutOrNull(2000L) {
                if (inChina!!) {
                    serverVipApi.testStatistics(TestRequest())
                } else {
                    serverAppApi.testStatistics(TestRequest())
                }
            }
            if (response == null) {
                connectServer.content(StatisticsResponse(inBlackList = false, useCloudCompress = false))
                return@launch
            }
            if (response.useCloudCompress == null) {
                //服务端没有告知是否使用云端压缩
                //通过ip地址检测地区
                response.useCloudCompress = inChina
            }
            connectServer.content(response)
        }
    }
}