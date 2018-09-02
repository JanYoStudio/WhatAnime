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
import vip.mystery0.rxpackagedata.PackageData
import vip.mystery0.rxpackagedata.rx.RxObservable
import vip.mystery0.rxpackagedata.rx.RxObserver
import vip.mystery0.tools.utils.FileTools
import java.io.File
import java.util.*

object LocalAnimationDataSource : AnimationDateSource {
	private val historyService: HistoryService = HistoryServiceImpl

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, file: File, filter: String?) {
		RxObservable<Animation>()
				.doThings {
					val animationHistory = historyService.queryHistoryByOriginPathAndFilter(file.absolutePath, filter)
					if (animationHistory == null) {
						RemoteAnimationDataSource.queryAnimationByImage(animationLiveData, file, filter)
						return@doThings
					}
					val animation = GsonFactory.gson.fromJson<Animation>(animationHistory.result, Animation::class.java)
					it.onFinish(animation)
				}
				.subscribe(object : RxObserver<Animation>() {
					override fun onFinish(data: Animation?) {
						if (data == null||data.docs.isEmpty())
							animationLiveData.value = PackageData.empty()
						else
							animationLiveData.value = PackageData.content(data)
					}

					override fun onError(e: Throwable) {
						animationLiveData.value = PackageData.error(e)
					}
				})
	}

	fun saveHistory(animationLiveData: MutableLiveData<PackageData<Animation>>, file: File, filter: String?, animation: Animation) {
		val animationHistory = AnimationHistory()
		animationHistory.originPath = file.absolutePath
		val saveFile = FileUtil.getCacheFile(file)
		if (saveFile == null) {
			animationLiveData.value = PackageData.error(Exception(StringConstant.hint_cache_make_dir_error))
			return
		}
		when (FileTools.copyFile(file.absolutePath, saveFile.absolutePath)) {
			FileTools.MAKE_DIR_ERROR -> {
				animationLiveData.value = PackageData.error(Exception(StringConstant.hint_cache_make_dir_error))
				return
			}
			FileTools.FILE_NOT_EXIST -> {
				animationLiveData.value = PackageData.error(Exception(StringConstant.hint_origin_file_null))
				return
			}
			FileTools.ERROR -> {
				animationLiveData.value = PackageData.error(Exception(StringConstant.hint_file_copy_error))
				return
			}
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

	fun queryAllHistory(animationHistoryLiveData: MutableLiveData<PackageData<List<AnimationHistory>>>) {
		RxObservable<List<AnimationHistory>>()
				.doThings {
					it.onFinish(historyService.queryAllHistory())
				}
				.subscribe(object : RxObserver<List<AnimationHistory>>() {
					override fun onFinish(data: List<AnimationHistory>?) {
						if (data == null || data.isEmpty())
							animationHistoryLiveData.value = PackageData.empty()
						else
							animationHistoryLiveData.value = PackageData.content(data)
					}

					override fun onError(e: Throwable) {
						animationHistoryLiveData.value = PackageData.error(e)
					}
				})
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		RxObservable<Boolean>()
				.doThings {
					val result = historyService.delete(animationHistory)
					if (result == 1)
						it.onFinish(true)
					else
						it.onFinish(false)
				}
				.subscribe(object : RxObserver<Boolean>() {
					override fun onFinish(data: Boolean?) {
						if (data != null)
							listener.invoke(data)
					}

					override fun onError(e: Throwable) {
						e.printStackTrace()
						listener.invoke(false)
					}
				})
	}
}
