package pw.janyo.whatanime.repository.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import pw.janyo.whatanime.model.AnimationHistory

@Dao
interface HistoryDao {
    @Insert
    fun saveHistory(animationHistory: AnimationHistory): Long

    @Query("SELECT * FROM tb_animation_history where id = :historyId LIMIT 1")
    fun getById(historyId: Int): AnimationHistory?

    @Query("DELETE FROM tb_animation_history where id = :historyId")
    fun delete(historyId: Int): Int

    @Query("SELECT * FROM tb_animation_history")
    fun queryAllHistory(): List<AnimationHistory>

    @Update
    fun update(animationHistory: AnimationHistory): Int

    @Query("SELECT * FROM tb_animation_history WHERE origin_path = :originPath LIMIT 1")
    fun queryHistoryByOriginPath(originPath: String): AnimationHistory?
}