package pw.janyo.whatanime.repository

import android.content.Intent
import com.zhihu.matisse.Matisse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.janyo.whatanime.R
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.utils.cloneUriToFile
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.error
import vip.mystery0.tools.ResourceException
import java.io.File

object MainRepository {
	fun search(file: File, filter: String?, mainViewModel: MainViewModel) {
		mainViewModel.resultList.value = PackageData.loading()
		GlobalScope.launch(CoroutineExceptionHandler { _, throwable -> mainViewModel.resultList.error(throwable) }) {
			val animation = LocalAnimationDataSource.queryAnimationByImage(file, filter)
			if (animation.quota != -987654 && animation.quota_ttl != -987654) {
				val searchQuota = SearchQuota()
				searchQuota.quota = animation.quota
				searchQuota.quota_ttl = animation.quota_ttl
				mainViewModel.quota.content(searchQuota)
			}
			mainViewModel.resultList.content(animation)
		}
	}

	fun showQuota(mainViewModel: MainViewModel) {
		GlobalScope.launch(CoroutineExceptionHandler { _, throwable -> mainViewModel.quota.error(throwable) }) {
			mainViewModel.quota.content(RemoteAnimationDataSource.showQuota())
		}
	}

	fun parseImageFileByMatisse(mainViewModel: MainViewModel, data: Intent) {
		GlobalScope.launch(CoroutineExceptionHandler { _, throwable -> mainViewModel.imageFile.error(throwable) }) {
			val fileList = Matisse.obtainPathResult(data)
			if (fileList.isNotEmpty()) {
				mainViewModel.imageFile.content(File(fileList[0]))
			} else {
				throw ResourceException(R.string.hint_select_file_path_null)
			}
		}
	}

	fun parseImageFile(mainViewModel: MainViewModel, data: Intent) {
		GlobalScope.launch(CoroutineExceptionHandler { _, throwable -> mainViewModel.imageFile.error(throwable) }) {
			val file = data.data!!.cloneUriToFile()
			if (file != null && file.exists()) {
				mainViewModel.imageFile.content(file)
			} else {
				throw ResourceException(R.string.hint_select_file_path_null)
			}
		}
	}
}