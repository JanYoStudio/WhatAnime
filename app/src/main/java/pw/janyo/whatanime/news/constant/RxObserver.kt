package pw.janyo.whatanime.news.constant

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class RxObserver<T> : Observer<T> {
	override fun onSubscribe(d: Disposable) {
	}

	override fun onNext(t: T) {
	}

	override fun onError(e: Throwable) {
	}

	override fun onComplete() {
	}
}