package com.example.minecraftclicker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minecraft_clicker.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(SeedAccounts)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private object SeedAccounts : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("INSERT INTO users (name, password, role) VALUES ('admin', 'admin123', 'ADMIN')")
            db.execSQL("INSERT INTO users (name, password, role) VALUES ('jogador', '1234', 'PLAYER')")
        }
    }
}
