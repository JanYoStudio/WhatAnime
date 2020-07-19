package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.KoinComponent
import org.koin.core.inject
import pw.janyo.whatanime.R
import pw.janyo.whatanime.api.ServerApi
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.model.request.TestRequest
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.launch
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.utils.isConnectInternet

class TestViewModel : ViewModel(), KoinComponent {
	private val serverApi: ServerApi by inject()

	val connectServer = MediatorLiveData<PackageData<Pair<Boolean, Boolean>>>()

	fun doTest() {
		launch(connectServer) {
			if (!Configure.enableCloudCompress) {
				connectServer.content(Pair(first = false, second = false))
				return@launch
			}
			if (!isConnectInternet()) {
				throw ResourceException(R.string.hint_no_network)
			}
			val response = withTimeoutOrNull(4000L) {
				serverApi.testOp(TestRequest())
			}
			if (response?.isSuccessful != true) connectServer.content(Pair(first = false, second = false))
			val inBlackList = !response?.data!!
			connectServer.content(Pair(first = !inBlackList, second = inBlackList))
		}
	}
}