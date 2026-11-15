package com.example.minecraftclicker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        User::class,
        UpgradeEntity::class,
        ProgressoJogador::class,
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun upgradeDao(): UpgradeDao
    abstract fun progressoJogadorDao(): ProgressoJogadorDao

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
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(SeedAccounts)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    internal object MIGRATION_3_4 : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS progresso_jogador (
                    jogadorId INTEGER NOT NULL PRIMARY KEY,
                    dinheiro INTEGER NOT NULL,
                    dinheiroTotalGanho INTEGER NOT NULL,
                    blocosQuebrados INTEGER NOT NULL,
                    cliques INTEGER NOT NULL,
                    minerioId TEXT NOT NULL,
                    minerioNome TEXT NOT NULL,
                    minerioValor INTEGER NOT NULL,
                    vidaAtual INTEGER NOT NULL,
                    vidaMaxima INTEGER NOT NULL
                )
            """.trimIndent())
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
