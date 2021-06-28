package pw.janyo.whatanime.module

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import pw.janyo.whatanime.repository.local.db.DB
import pw.janyo.whatanime.repository.local.service.HistoryService
import pw.janyo.whatanime.repository.local.service.HistoryServiceImpl

private const val DATABASE_NAME = "db_what_anime"

private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table tb_animation_history rename to _tb_animation_history")
        database.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT NOT NULL, `cache_path` TEXT NOT NULL, `animation_result` TEXT NOT NULL, `animation_time` INTEGER NOT NULL, `animation_title` TEXT NOT NULL, `animation_filter` TEXT , `base64` TEXT NOT NULL)")
        database.execSQL("insert into tb_animation_history select *,' ' from _tb_animation_history")
        database.execSQL("drop table _tb_animation_history")
    }
}
private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table tb_animation_history rename to _tb_animation_history")
        database.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT, `cache_path` TEXT, `animation_result` TEXT, `animation_time` INTEGER NOT NULL, `animation_title` TEXT, `animation_filter` TEXT)")
        database.execSQL("insert into tb_animation_history select id, origin_path, cache_path, animation_result, animation_time, animation_title, animation_filter from _tb_animation_history")
        database.execSQL("drop table _tb_animation_history")
    }
}
private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table tb_animation_history rename to _tb_animation_history")
        database.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT NOT NULL, `cache_path` TEXT NOT NULL, `animation_result` TEXT NOT NULL, `animation_time` INTEGER NOT NULL, `animation_title` TEXT NOT NULL, `animation_filter` TEXT)")
        database.execSQL("insert into tb_animation_history select id, origin_path, cache_path, animation_result, animation_time, animation_title, animation_filter from _tb_animation_history")
        database.execSQL("drop table _tb_animation_history")
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext().applicationContext, DB::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .build()
    }
    single {
        get<DB>().getHistoryDao()
    }
    single<HistoryService> {
        HistoryServiceImpl()
    }
}