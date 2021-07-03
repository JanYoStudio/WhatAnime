package pw.janyo.whatanime.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

abstract class ComposeViewModel : ViewModel() {
    private val _isRefreshing = MutableLiveData(false)
    private val _errorMessage = MutableLiveData<String?>(null)
    private val _exception = MutableLiveData<Throwable?>(null)

    val refreshData: LiveData<Boolean>
        get() = _isRefreshing
    val errorMessageData: LiveData<String?>
        get() = _errorMessage
    val exceptionData: LiveData<Throwable?>
        get() = _exception

    fun refreshState(state: Boolean) {
        _isRefreshing.postValue(state)
    }

    fun errorMessageState(message: String) {
        _errorMessage.postValue(message)
    }

    fun exceptionState(throwable: Throwable) {
        _exception.postValue(throwable)
        throwable.message?.let {
            errorMessageState(it)
        }
    }

    fun clear() {
        _exception.postValue(null)
        _errorMessage.postValue(null)
    }

    protected fun launch(action: suspend () -> Unit) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            exceptionState(throwable)
        }) {
            action()
        }
    }

    protected fun launchLoadData(action: suspend () -> Unit) {
        launch {
            refreshState(true)
            action()
            refreshState(false)
        }
    }
}