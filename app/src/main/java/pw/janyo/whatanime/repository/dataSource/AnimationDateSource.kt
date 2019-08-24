package pw.janyo.whatanime.repository.dataSource

import androidx.lifecycle.MutableLiveData
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.SearchQuota
import vip.mystery0.rx.PackageData
import java.io.File

interface AnimationDateSource {
	fun queryAnimationByImage(animationLiveData: MutableLiveData<PackageData<Animation>>, quotaLiveData: MutableLiveData<PackageData<SearchQuota>>, file: File, filter: String?)
}