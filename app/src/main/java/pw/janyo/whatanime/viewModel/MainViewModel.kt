package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import vip.mystery0.rx.PackageData
import java.io.File

class MainViewModel : ViewModel() {
	val imageFile = MutableLiveData<File>()
	val resultList = MutableLiveData<PackageData<Animation>>()
	val isShowDetail = MutableLiveData<Boolean>()
	val quota = MutableLiveData<PackageData<SearchQuota>>()
}