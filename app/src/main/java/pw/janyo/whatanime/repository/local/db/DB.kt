package pw.janyo.whatanime.repository.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.local.dao.HistoryDao

@Database(entities = [(AnimationHistory::class)], version = 3)
abstract class DB : RoomDatabase() {
	abstract fun getHistoryDao(): HistoryDao
}