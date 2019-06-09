package pw.janyo.whatanime.repository.local

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pw.janyo.whatanime.config.Configure
import pw.janyo.whatanime.constant.StringConstant
import pw.janyo.whatanime.factory.GsonFactory
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.dataSource.AnimationDateSource
import pw.janyo.whatanime.repository.local.service.HistoryService
import pw.janyo.whatanime.repository.local.service.HistoryServiceImpl
import pw.janyo.whatanime.repository.remote.RemoteAnimationDataSource
import pw.janyo.whatanime.utils.FileUtil
import vip.mystery0.rx.OnlyCompleteObserver
import vip.mystery0.rx.PackageData
import vip.mystery0.tools.ToolsException
import vip.mystery0.tools.doByTry
import vip.mystery0.tools.utils.FileTools
import java.io.File
import java.util.*

object LocalAnimationDataSource : AnimationDateSource {
	private val historyService: HistoryService = HistoryServiceImpl

	override fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, file: File, filter: String?) {
		Observable.create<String> {
			val animationHistory = historyService.queryHistoryByOriginPathAndFilter(file.absolutePath, filter)
			if (animationHistory != null)
				it.onNext(animationHistory.result)
			it.onComplete()
		}
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.computation())
				.map { GsonFactory.gson.fromJson<Animation>(it, Animation::class.java) }
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : OnlyCompleteObserver<Animation>() {
					override fun onError(e: Throwable) {
						animationLiveData.value = PackageData.error(e)
					}

					override fun onFinish(data: Animation?) {
						if (data == null) {
							RemoteAnimationDataSource.queryAnimationByImage(animationLiveData, file, filter)
							return
						}
						if (data.docs.isEmpty())
							animationLiveData.value = PackageData.empty()
						else {
							if (Configure.hideSex)
								data.docs = data.docs.filter { !it.is_adult }
							animationLiveData.value = PackageData.content(data)
						}
					}
				})
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
		animationHistory.result = GsonFactory.gson.toJson(animation)
		animationHistory.time = Calendar.getInstance().timeInMillis
		if (animation.docs.isNotEmpty())
			animationHistory.title = animation.docs[0].title_native
		else
			animationHistory.title = StringConstant.hint_no_result
		animationHistory.filter = filter
		historyService.saveHistory(animationHistory)
	}

	fun queryAllHistory(animationHistoryLiveData: MutableLiveData<PackageData<List<AnimationHistory>>>) {
		Observable.create<List<AnimationHistory>> {
			it.onNext(historyService.queryAllHistory())
			it.onComplete()
		}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : OnlyCompleteObserver<List<AnimationHistory>>() {
					override fun onError(e: Throwable) {
						animationHistoryLiveData.value = PackageData.error(e)
					}

					override fun onFinish(data: List<AnimationHistory>?) {
						if (data == null || data.isEmpty())
							animationHistoryLiveData.value = PackageData.empty()
						else
							animationHistoryLiveData.value = PackageData.content(data)
					}
				})
	}

	fun deleteHistory(animationHistory: AnimationHistory, listener: (Boolean) -> Unit) {
		Observable.create<Boolean> {
			val result = historyService.delete(animationHistory)
			it.onNext(result == 1)
			it.onComplete()
		}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object : OnlyCompleteObserver<Boolean>() {
					override fun onError(e: Throwable) {
						e.printStackTrace()
						listener.invoke(false)
					}

					override fun onFinish(data: Boolean?) {
						if (data != null)
							listener.invoke(data)
					}
				})
	}
}
