package pw.janyo.whatanime.repository.dataSource

import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.model.Animation
import java.io.File

interface AnimationDateSource {
	fun queryAnimationByImage(animationLiveData: MutableLiveData<Animation>, messageLiveData: MutableLiveData<String>, file: File, filter: String?)
}