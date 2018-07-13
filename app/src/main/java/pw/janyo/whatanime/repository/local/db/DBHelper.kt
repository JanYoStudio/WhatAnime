package pw.janyo.whatanime.repository.local.db

import android.content.Context
import androidx.room.Room

object DBHelper {
	private const val DATABASE_NAME = "db_what_anime"
	lateinit var db: DB

	fun init(context: Context) {
		db = Room.databaseBuilder(context.applicationContext, DB::class.java, DATABASE_NAME).build()
	}
}