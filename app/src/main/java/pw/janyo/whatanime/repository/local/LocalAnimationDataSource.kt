package pw.janyo.whatanime.repository.local

import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.service.HistoryService
import pw.janyo.whatanime.repository.local.service.HistoryServiceImpl
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import vip.mystery0.rx.PackageData
import vip.mystery0.rx.content
import vip.mystery0.rx.empty
import vip.mystery0.tools.ToolsException
import vip.mystery0.tools.doByTry
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.factory.toJson
import vip.mystery0.tools.utils.FileTools
import java.io.File
import java.util.*

object LocalAnimationDataSource : AnimationDateSource {
	private val historyService: HistoryService = HistoryServiceImpl

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, quotaLiveData: MutableLiveData<PackageData<SearchQuota>>, file: File, filter: String?) {
		val animationHistory = historyService.queryHistoryByOriginPathAndFilter(file.absolutePath, filter)
		if (animationHistory != null) {
			val animation = animationHistory.result.fromJson<Animation>()
			if (animation.docs.isEmpty()) {
				animationLiveData.empty()
			}
			if (Configure.hideSex)
				animation.docs = animation.docs.filter { !it.is_adult }
			animationLiveData.content(animation)
			return
		}
		RemoteAnimationDataSource.queryAnimationByImage(animationLiveData, quotaLiveData, file, filter)
	}

	fun saveHistory(animationLiveData: MutableLiveData<PackageData<Animation>>, file: File, filter: String?, animation: Animation) {
		val animationHistory = AnimationHistory()
		animationHistory.originPath = file.absolutePath
		val saveFile = FileUtil.getCacheFile(file)
		if (saveFile == null) {
			animationLiveData.postValue(PackageData.error(Exception(StringConstant.hint_cache_make_dir_error)))
			return
		}
		val exception = doByTry { FileTools.instance.copyFile(file, saveFile) }
		if (exception != null) {
			if (exception !is ToolsException) {
				animationLiveData.postValue(PackageData.error(Exception(StringConstant.hint_file_copy_error)))
				return
			}
			when (exception.code) {
				ToolsException.MAKE_DIR_ERROR -> animationLiveData.postValue(PackageData.error(Exception(StringConstant.hint_cache_make_dir_error)))
				ToolsException.FILE_NOT_EXIST -> animationLiveData.postValue(PackageData.error(Exception(StringConstant.hint_origin_file_null)))
			}
			return
		}
		animationHistory.cachePath = saveFile.absolutePath
		animationHistory.result = animation.toJson()
		animationHistory.time = Calendar.getInstance().timeInMillis
		if (animation.docs.isNotEmpty())
			animationHistory.title = animation.docs[0].title_native
		else
			animationHistory.title = StringConstant.hint_no_result
		animationHistory.filter = filter
		historyService.saveHistory(animationHistory)
	}

	fun queryAllHistory(animationHistoryLiveData: MutableLiveData<PackageData<List<AnimationHistory>>>) {
		val list = historyService.queryAllHistory()
		if (list.isEmpty())
			animationHistoryLiveData.empty()
		else
			animationHistoryLiveData.content(list)
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		listener(historyService.delete(animationHistory) == 1)
	}
}
