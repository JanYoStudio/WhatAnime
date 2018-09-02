package pw.janyo.whatanime.repository.dataSource

import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.model.Animation
import vip.mystery0.rxpackagedata.PackageData
import java.io.File

interface AnimationDateSource {
	fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, file: File, filter: String?)
}