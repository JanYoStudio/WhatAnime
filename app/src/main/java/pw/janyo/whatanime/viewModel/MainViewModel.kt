package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pw.janyo.whatanime.model.Animation
import vip.mystery0.rxpackagedata.PackageData
import java.io.File

class MainViewModel : ViewModel() {
	val imageFile = MutableLiveData<File>()
	val resultList = MutableLiveData<PackageData<Animation>>()
	val isShowDetail = MutableLiveData<Boolean>()
}