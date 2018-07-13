package pw.janyo.whatanime.repository.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pw.janyo.whatanime.model.AnimationHistory

@Dao
interface HistoryDao {
	@Insert
	fun saveHistory(vararg animationHistory: AnimationHistory): Long

	@Delete
	fun delete(vararg animationHistory: AnimationHistory): Long

	@Query("SELECT * FROM tb_animation_history")
	fun queryAllHistory(): LiveData<AnimationHistory>

	@Update
	fun update(vararg animationHistory: AnimationHistory): Long

	@Query("SELECT * FROM tb_animation_history WHERE origin_path = :originPath")
	fun queryHistoryByOriginPath(originPath: String): LiveData<AnimationHistory>
}