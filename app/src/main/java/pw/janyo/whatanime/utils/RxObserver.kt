package pw.janyo.whatanime.utils

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class RxObserver<T> : Observer<T> {
	private var data: T? = null
	override fun onSubscribe(d: Disposable) {
	}

	override fun onNext(t: T) {
		data = t
	}

	override fun onComplete() {
		onFinish(data)
	}

	abstract fun onFinish(data: T?)
}