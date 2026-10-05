package pw.janyo.whatanime.module

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.viewmodel.DetailViewModel
import pw.janyo.whatanime.viewmodel.HistoryViewModel
import pw.janyo.whatanime.viewmodel.MainViewModel
import pw.janyo.whatanime.viewmodel.SettingsViewModel

val viewModelModule = module {
    viewModel {
        object : ComposeViewModel() {
            init {
                launchToInit()
            }
        }
    }
    viewModel { MainViewModel(get(), get(), savedStateHandle = get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { DetailViewModel(get(), get()) }
    viewModel { SettingsViewModel() }
}