package pw.janyo.whatanime.repository.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DBHelper {
	private const val DATABASE_NAME = "db_what_anime"
	lateinit var db: DB

	private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
		override fun migrate(database: SupportSQLiteDatabase) {
			database.execSQL("alter table tb_animation_history rename to _tb_animation_history")
			database.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT NOT NULL, `cache_path` TEXT NOT NULL, `animation_result` TEXT NOT NULL, `animation_time` INTEGER NOT NULL, `animation_title` TEXT NOT NULL, `animation_filter` TEXT , `base64` TEXT NOT NULL)")
			database.execSQL("insert into tb_animation_history select *,' ' from _tb_animation_history")
			database.execSQL("drop table _tb_animation_history")
		}
	}

	fun init(context: Context) {
		db = Room.databaseBuilder(context.applicationContext, DB::class.java, DATABASE_NAME)
				.addMigrations(MIGRATION_1_2)
				.build()
	}
}