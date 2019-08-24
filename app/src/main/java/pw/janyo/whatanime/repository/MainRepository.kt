package pw.janyo.whatanime.repository

import android.content.Intent
import com.zhihu.matisse.Matisse
import pw.janyo.whatanime.R
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.rx.*
import vip.mystery0.tools.ResourceException
import java.io.File

object MainRepository {
	fun search(file: File, filter: String?, mainViewModel: MainViewModel) {
		mainViewModel.resultList.value = PackageData.loading()
		DataManager.instance().doRequest {
			LocalAnimationDataSource.queryAnimationByImage(mainViewModel.resultList, mainViewModel.quota, file, filter)
		}
	}

	fun showQuota(mainViewModel: MainViewModel) {
		DataManager.instance().doRequest {
			RemoteAnimationDataSource.showQuota(mainViewModel.quota)
		}
	}

	fun parseImageFileByMatisse(mainViewModel: MainViewModel, data: Intent) {
		DataManager.instance().doRequest(mainViewModel.imageFile) {
			val fileList = Matisse.obtainPathResult(data)
			if (fileList.isNotEmpty()) {
				mainViewModel.imageFile.content(File(fileList[0]))
			} else {
				mainViewModel.imageFile.error(ResourceException(R.string.hint_select_file_path_null))
			}
		}
	}

	fun parseImageFile(mainViewModel: MainViewModel, data: Intent) {
		DataManager.instance().doRequest(mainViewModel.imageFile) {
			val file = FileUtil.cloneUriToFile(data.data!!)
			if (file != null && file.exists()) {
				mainViewModel.imageFile.content(file)
			} else {
				mainViewModel.imageFile.error(ResourceException(R.string.hint_select_file_path_null))
			}
		}
	}
}