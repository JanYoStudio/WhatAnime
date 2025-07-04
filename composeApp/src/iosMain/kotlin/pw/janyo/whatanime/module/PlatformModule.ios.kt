package pw.janyo.whatanime.module

import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import pw.janyo.whatanime.db.AppDatabase

actual fun platformModule(): Module = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
            .setDriver(NativeSQLiteDriver())
    }
}