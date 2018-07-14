package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pw.janyo.whatanime.model.Animation
import java.io.File

class MainViewModel : ViewModel() {
	var imageFile = MutableLiveData<File>()
	var resultList = MutableLiveData<Animation>()
	var message = MutableLiveData<String>()
}