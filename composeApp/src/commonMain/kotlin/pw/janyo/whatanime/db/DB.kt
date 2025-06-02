package pw.janyo.whatanime.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import pw.janyo.whatanime.db.dao.HistoryDao
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.module.AppDatabaseConstructor

@Database(entities = [(AnimationHistory::class)], version = 5)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun getHistoryDao(): HistoryDao
}