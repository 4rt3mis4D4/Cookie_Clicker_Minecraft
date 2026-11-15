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
        HistoricoMineracao::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun upgradeDao(): UpgradeDao
    abstract fun progressoJogadorDao(): ProgressoJogadorDao
    abstract fun historicoMineracaoDao(): HistoricoMineracaoDao

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
                    .addMigrations(
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(SeedAccounts)
                    .build()

                INSTANCE = instance
                instance
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

        internal object MIGRATION_4_5 : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS historico_mineracao (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        jogadorId INTEGER NOT NULL,
                        minerioId TEXT NOT NULL,
                        minerioNome TEXT NOT NULL,
                        quantidade INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_historico_mineracao_jogadorId
                    ON historico_mineracao (jogadorId)
                """.trimIndent())

                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS
                    index_historico_mineracao_minerioId
                    ON historico_mineracao (minerioId)
                """.trimIndent())
            }
        }
    }

    private object SeedAccounts : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            db.execSQL(
                "INSERT INTO users (name, password, role) VALUES ('admin', 'admin123', 'ADMIN')"
            )

            db.execSQL(
                "INSERT INTO users (name, password, role) VALUES ('jogador', '1234', 'PLAYER')"
            )
        }
    }
}
