package pw.janyo.whatanime.repository.local.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.repository.local.db.DBHelper
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.utils.RxObservable
import pw.janyo.whatanime.utils.RxObserver
import vip.mystery0.logs.Logs

object HistoryServiceImpl : HistoryService {

	private val historyDao = DBHelper.db.getHistoryDao()

	override fun saveHistory(animationHistory: AnimationHistory): LiveData<Long> {
		val longLiveData = MutableLiveData<Long>()
		RxObservable<Long>()
				.doThingsOnThread {
					try {
						it.onFinish(historyDao.saveHistory(animationHistory))
					} catch (e: Exception) {
						it.onError(e)
					}
				}
				.subscribe(object : RxObserver<Long>() {
					override fun onFinish(data: Long?) {
						if (data != null)
							longLiveData.value = data
					}

					override fun onError(e: Throwable) {
						Logs.wtf("onError: ", e)
					}
				})
		return longLiveData
	}

	override fun delete(animationHistory: AnimationHistory): LiveData<Long> {
		val longLiveData = MutableLiveData<Long>()
		RxObservable<Long>()
				.doThingsOnThread {
					try {
						it.onFinish(historyDao.delete(animationHistory))
					} catch (e: Exception) {
						it.onError(e)
					}
				}
				.subscribe(object : RxObserver<Long>() {
					override fun onFinish(data: Long?) {
						if (data != null)
							longLiveData.value = data
					}

					override fun onError(e: Throwable) {
						Logs.wtf("onError: ", e)
					}
				})
		return longLiveData
	}

	override fun queryAllHistory(): LiveData<AnimationHistory> = historyDao.queryAllHistory()

	override fun update(animationHistory: AnimationHistory): LiveData<Long> {
		val longLiveData = MutableLiveData<Long>()
		RxObservable<Long>()
				.doThingsOnThread {
					try {
						it.onFinish(historyDao.update(animationHistory))
					} catch (e: Exception) {
						it.onError(e)
					}
				}
				.subscribe(object : RxObserver<Long>() {
					override fun onFinish(data: Long?) {
						if (data != null)
							longLiveData.value = data
					}

					override fun onError(e: Throwable) {
						Logs.wtf("onError: ", e)
					}
				})
		return longLiveData
	}

	override fun queryHistoryByOriginPath(originPath: String): LiveData<AnimationHistory> = historyDao.queryHistoryByOriginPath(originPath)
}