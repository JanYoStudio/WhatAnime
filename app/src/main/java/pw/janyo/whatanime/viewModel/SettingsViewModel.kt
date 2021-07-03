package pw.janyo.whatanime.viewModel

import androidx.lifecycle.MutableLiveData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pw.janyo.whatanime.base.ComposeViewModel
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.repository.AnimationRepository

class SettingsViewModel : ComposeViewModel(), KoinComponent {
    private val animationRepository: AnimationRepository by inject()

    val searchQuota = MutableLiveData<SearchQuota>()

    fun showQuota() {
        launchLoadData {
            searchQuota.postValue(animationRepository.showQuota())
        }
    }
}