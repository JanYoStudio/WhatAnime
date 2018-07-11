package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class SearchViewModel:ViewModel() {
	var searchImageFile=MutableLiveData<File>()
	get() {

	}
	private set
}