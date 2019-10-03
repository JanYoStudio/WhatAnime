package pw.janyo.whatanime.viewModel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zhihu.matisse.Matisse
import pw.janyo.whatanime.R
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.utils.cloneUriToFile
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.launch
import vip.mystery0.rx.loading
import vip.mystery0.tools.ResourceException
import java.io.File

class MainViewModel : ViewModel() {
	val imageFile = MutableLiveData<PackageData<File>>()
	val resultList = MutableLiveData<PackageData<Animation>>()
	val isShowDetail = MutableLiveData<Boolean>()
	val quota = MutableLiveData<PackageData<SearchQuota>>()

	fun search(file: File, filter: String?) {
		resultList.loading()
		launch(resultList) {
			val animation = LocalAnimationDataSource.queryAnimationByImage(file, filter)
			if (animation.quota != -987654 && animation.quota_ttl != -987654) {
				val searchQuota = SearchQuota()
				searchQuota.quota = animation.quota
				searchQuota.quota_ttl = animation.quota_ttl
				quota.content(searchQuota)
			}
			resultList.content(animation)
		}
	}

	fun showQuota() {
		launch(quota) {
			quota.content(RemoteAnimationDataSource.showQuota())
		}
	}

	fun parseImageFileByMatisse(data: Intent) {
		launch(imageFile) {
			val fileList = Matisse.obtainPathResult(data)
			if (fileList.isNotEmpty()) {
				imageFile.content(File(fileList[0]))
			} else {
				throw ResourceException(R.string.hint_select_file_path_null)
			}
		}
	}

	fun parseImageFile(data: Intent) {
		launch(imageFile) {
			val file = data.data!!.cloneUriToFile()
			if (file != null && file.exists()) {
				imageFile.content(file)
			} else {
				throw ResourceException(R.string.hint_select_file_path_null)
			}
		}
	}
}