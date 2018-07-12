package pw.janyo.whatanime.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.repository.SearchRepository
import java.io.File

class MainViewModel : ViewModel() {
	private var searchImageFile: LiveData<File>? = null
	private var searchResultList: LiveData<ArrayList<Docs>>? = null

	fun getSearchFile(path: String): LiveData<File> {
		searchImageFile = SearchRepository.convertUriToFile(path)
		return searchImageFile!!
	}

	fun getSearchResultList(file: File): LiveData<ArrayList<Docs>> {
		searchResultList = SearchRepository.search(file)
		return searchResultList!!
	}
}