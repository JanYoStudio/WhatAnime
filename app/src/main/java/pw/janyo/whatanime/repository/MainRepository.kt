package pw.janyo.whatanime.repository

import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageData
import java.io.File

object MainRepository {
	fun search(file: File, filter: String?, mainViewModel: MainViewModel) {
		mainViewModel.resultList.value = PackageData.loading()
		LocalAnimationDataSource.queryAnimationByImage(mainViewModel.resultList, file, filter)
	}

	fun showQuota(mainViewModel: MainViewModel) {
		RemoteAnimationDataSource.showQuota(mainViewModel.quota)
	}
}