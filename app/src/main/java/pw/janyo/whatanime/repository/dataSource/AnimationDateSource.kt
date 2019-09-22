package pw.janyo.whatanime.repository.dataSource

import pw.janyo.whatanime.model.Animation
import java.io.File

interface AnimationDateSource {
	suspend fun queryAnimationByImage(file: File, filter: String?): Animation
}