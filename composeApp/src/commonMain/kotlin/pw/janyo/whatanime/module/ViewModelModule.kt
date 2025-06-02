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
    viewModel { MainViewModel() }
    viewModel { HistoryViewModel() }
    viewModel { DetailViewModel() }
    viewModel { SettingsViewModel() }
}