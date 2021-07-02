package pw.janyo.whatanime.repository.local.dao

import androidx.room.*
import pw.janyo.whatanime.model.AnimationHistory

@Dao
interface HistoryDao {
    @Insert
    fun saveHistory(animationHistory: AnimationHistory): Long

    @Delete
    fun delete(animationHistory: AnimationHistory): Int

    @Query("SELECT * FROM tb_animation_history")
    fun queryAllHistory(): List<AnimationHistory>

    @Update
    fun update(animationHistory: AnimationHistory): Int

    @Query("SELECT * FROM tb_animation_history WHERE origin_path = :originPath LIMIT 1")
    fun queryHistoryByOriginPath(originPath: String): AnimationHistory?
}