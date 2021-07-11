package pw.janyo.whatanime.module

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pw.janyo.whatanime.viewModel.*

val viewModelModule = module {
    viewModel {
        MainViewModel()
    }
    viewModel {
        HistoryViewModel()
    }
    viewModel {
        DetailViewModel()
    }
    viewModel {
        SettingsViewModel()
    }
}