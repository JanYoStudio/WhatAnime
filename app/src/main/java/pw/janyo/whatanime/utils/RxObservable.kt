package pw.janyo.whatanime.utils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RxObservable<T> {
	fun doThings(listener: (RxObservableEmitter<T>) -> Unit): Observable<T> = Observable.create<T> {
		val emitter = object : RxObservableEmitter<T> {
			override fun onError(error: Throwable) {
				it.onError(error)
			}

			override fun onFinish(data: T) {
				it.onNext(data)
				it.onComplete()
			}
		}
		listener(emitter)
	}
			.subscribeOn(Schedulers.newThread())
			.unsubscribeOn(Schedulers.newThread())
			.observeOn(AndroidSchedulers.mainThread())

	fun doThingsOnThread(listener: (RxObservableEmitter<T>) -> Unit): Observable<T> = Observable.create<T> {
		val emitter = object : RxObservableEmitter<T> {
			override fun onError(error: Throwable) {
				it.onError(error)
			}

			override fun onFinish(data: T) {
				it.onNext(data)
				it.onComplete()
			}
		}
		listener(emitter)
	}
			.subscribeOn(Schedulers.newThread())
			.unsubscribeOn(Schedulers.newThread())
			.observeOn(Schedulers.newThread())

	interface RxObservableEmitter<T> {
		fun onError(error: Throwable)

		fun onFinish(data: T)
	}
}