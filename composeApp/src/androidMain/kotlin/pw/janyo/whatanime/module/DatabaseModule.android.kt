package pw.janyo.whatanime.module

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

import pw.janyo.whatanime.db.AppDatabase

fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    return Room.databaseBuilder(
        context = appContext,
        klass = AppDatabase::class.java,
        name = DATABASE_NAME
    )
}