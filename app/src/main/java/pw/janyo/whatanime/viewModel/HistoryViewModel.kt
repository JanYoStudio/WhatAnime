package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pw.janyo.whatanime.model.AnimationHistory

class HistoryViewModel : ViewModel() {
	var historyList = MutableLiveData<List<AnimationHistory>>()
	var message = MutableLiveData<String>()
}