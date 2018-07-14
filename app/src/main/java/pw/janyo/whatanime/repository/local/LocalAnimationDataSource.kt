package pw.janyo.whatanime.repository.local

import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.factory.GsonFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.service.HistoryService
import pw.janyo.whatanime.repository.local.service.HistoryServiceImpl
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import pw.janyo.whatanime.utils.RxObservable
import pw.janyo.whatanime.utils.RxObserver
import vip.mystery0.logs.Logs
import java.io.File
import java.util.*

object LocalAnimationDataSource : AnimationDateSource {

	private val historyService: HistoryService = HistoryServiceImpl

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<Animation>, messageLiveData: MutableLiveData<String>, file: File, filter: String?) {
		RxObservable<Animation>()
				.doThings {
					try {
						val animationHistory = historyService.queryHistoryByOriginPathAndFilter(file.absolutePath, filter)
						if (animationHistory == null) {
							RemoteAnimationDataSource.queryAnimationByImage(animationLiveData, messageLiveData, file, filter)
							return@doThings
						}
						val animation = GsonFactory.gson.fromJson<Animation>(animationHistory.result, Animation::class.java)
						it.onFinish(animation)
					} catch (e: Exception) {
						it.onError(e)
					}
				}
				.subscribe(object : RxObserver<Animation>() {
					override fun onFinish(data: Animation?) {
						animationLiveData.value = data
					}

					override fun onError(e: Throwable) {
						messageLiveData.value = e.message
					}
				})
	}

	fun saveHistory(messageLiveData: MutableLiveData<String>, file: File, filter: String?, animation: Animation) {
		val animationHistory = AnimationHistory()
		animationHistory.originPath = file.absolutePath
		val saveFile = FileUtil.getCacheFile(file)
		if (saveFile == null) {
			messageLiveData.value = StringConstant.hint_cache_make_dir_error
			return
		}
		animationHistory.cachePath = saveFile.absolutePath
		animationHistory.result = GsonFactory.gson.toJson(animation)
		animationHistory.time = Calendar.getInstance().timeInMillis
		if (animation.docs.size > 0)
			animationHistory.title = animation.docs[0].title_native
		else
			animationHistory.title = StringConstant.hint_no_result
		animationHistory.filter = filter
		historyService.saveHistory(animationHistory)
	}
}