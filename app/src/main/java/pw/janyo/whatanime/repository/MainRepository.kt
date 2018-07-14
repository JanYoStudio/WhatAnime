package pw.janyo.whatanime.repository

import pw.janyo.whatanime.repository.local.LocalAnimationDataSource
import pw.janyo.whatanime.viewModel.MainViewModel
import java.io.File

object MainRepository {
	fun search(file: File, filter: String?, mainViewModel: MainViewModel) {
		LocalAnimationDataSource.queryAnimationByImage(mainViewModel.resultList, mainViewModel.message, file, filter)
	}
}