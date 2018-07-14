package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pw.janyo.whatanime.model.Animation
import java.io.File

class MainViewModel : ViewModel() {
	val imageFile = MutableLiveData<File>()
	val resultList = MutableLiveData<Animation>()
	val message = MutableLiveData<String>()
	val isShowDetail = MutableLiveData<Boolean>()
}