package pw.janyo.whatanime.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pw.janyo.whatanime.viewModel.HistoryViewModel
import pw.janyo.whatanime.viewModel.MainViewModel

val viewModelModule = module {
	viewModel {
		MainViewModel(get(), get())
	}
	viewModel {
		HistoryViewModel(get())
	}
}