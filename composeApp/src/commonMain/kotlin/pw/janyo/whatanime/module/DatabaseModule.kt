package pw.janyo.whatanime.module

import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.koin.dsl.module
import pw.janyo.whatanime.db.AppDatabase
import pw.janyo.whatanime.db.service.HistoryService
import pw.janyo.whatanime.db.service.HistoryServiceImpl

internal const val DATABASE_NAME = "db_what_anime"

val databaseModule = module {
    single {
        getRoomDatabase(get())
    }
    single {
        get<AppDatabase>().getHistoryDao()
    }
    single<HistoryService> {
        HistoryServiceImpl()
    }
}

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

private fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase = builder
    .addMigrations(MIGRATION_1_2)
    .addMigrations(MIGRATION_2_3)
    .addMigrations(MIGRATION_3_4)
    .addMigrations(MIGRATION_4_5)
    .build()

private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("alter table tb_animation_history rename to _tb_animation_history")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT NOT NULL, `cache_path` TEXT NOT NULL, `animation_result` TEXT NOT NULL, `animation_time` INTEGER NOT NULL, `animation_title` TEXT NOT NULL, `animation_filter` TEXT , `base64` TEXT NOT NULL)")
        connection.execSQL("insert into tb_animation_history select *,' ' from _tb_animation_history")
        connection.execSQL("drop table _tb_animation_history")
    }
}
private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("alter table tb_animation_history rename to _tb_animation_history")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT, `cache_path` TEXT, `animation_result` TEXT, `animation_time` INTEGER NOT NULL, `animation_title` TEXT, `animation_filter` TEXT)")
        connection.execSQL("insert into tb_animation_history select id, origin_path, cache_path, animation_result, animation_time, animation_title, animation_filter from _tb_animation_history")
        connection.execSQL("drop table _tb_animation_history")
    }
}
private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("alter table tb_animation_history rename to _tb_animation_history")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT NOT NULL, `cache_path` TEXT NOT NULL, `animation_result` TEXT NOT NULL, `animation_time` INTEGER NOT NULL, `animation_title` TEXT NOT NULL, `animation_filter` TEXT)")
        connection.execSQL("insert into tb_animation_history select id, origin_path, cache_path, animation_result, animation_time, animation_title, animation_filter from _tb_animation_history")
        connection.execSQL("drop table _tb_animation_history")
    }
}
private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("alter table tb_animation_history rename to _tb_animation_history")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_animation_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `origin_path` TEXT NOT NULL, `cache_path` TEXT NOT NULL, `animation_result` TEXT NOT NULL, `animation_time` INTEGER NOT NULL, `animation_title` TEXT NOT NULL, `animation_anilist_id` INTEGER NOT NULL, `animation_episode` TEXT NOT NULL, `animation_similarity` REAL NOT NULL)")
        connection.execSQL("insert into tb_animation_history select id, origin_path, cache_path, animation_result, animation_time, animation_title, 0, 'old', 0 from _tb_animation_history")
        connection.execSQL("drop table _tb_animation_history")
    }
}