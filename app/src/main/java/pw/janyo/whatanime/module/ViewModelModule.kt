package pw.janyo.whatanime.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pw.janyo.whatanime.viewModel.HistoryViewModel
import pw.janyo.whatanime.viewModel.MainViewModel
import pw.janyo.whatanime.viewModel.TestViewModel

val viewModelModule = module {
	viewModel {
		TestViewModel()
	}
	viewModel {
		MainViewModel()
	}
	viewModel {
		HistoryViewModel()
	}
}