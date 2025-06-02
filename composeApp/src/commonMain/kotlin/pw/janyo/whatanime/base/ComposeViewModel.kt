package pw.janyo.whatanime.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.allStringResources

abstract class ComposeViewModel : ViewModel(), KoinComponent {
    fun launchToInit() {
        viewModelScope.launch {
            Res.allStringResources.forEach {
                stringResourceMap[it.value] = getString(it.value)
            }
        }
    }

    protected fun StringResource.string(): String = stringResourceMap[this]!!

    companion object {
        private val stringResourceMap = hashMapOf<StringResource, String>()
    }
}