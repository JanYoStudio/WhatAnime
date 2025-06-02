package pw.janyo.whatanime.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import pw.janyo.whatanime.model.AnimationHistory

@Dao
interface HistoryDao {
    @Insert
    suspend fun saveHistory(animationHistory: AnimationHistory): Long

    @Query("SELECT * FROM tb_animation_history where id = :historyId LIMIT 1")
    suspend fun getById(historyId: Int): AnimationHistory?

    @Query("DELETE FROM tb_animation_history where id = :historyId")
    suspend fun delete(historyId: Int): Int

    @Query("SELECT * FROM tb_animation_history")
    suspend fun queryAllHistory(): List<AnimationHistory>

    @Update
    suspend fun update(animationHistory: AnimationHistory): Int

    @Query("SELECT * FROM tb_animation_history WHERE origin_path = :originPath LIMIT 1")
    suspend fun queryHistoryByOriginPath(originPath: String): AnimationHistory?
}