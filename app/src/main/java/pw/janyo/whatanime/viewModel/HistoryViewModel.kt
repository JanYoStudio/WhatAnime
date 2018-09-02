package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pw.janyo.whatanime.model.AnimationHistory
import vip.mystery0.rxpackagedata.PackageData

class HistoryViewModel : ViewModel() {
	var historyList = MutableLiveData<PackageData<List<AnimationHistory>>>()
}