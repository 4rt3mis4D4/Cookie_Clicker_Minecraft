package com.example.minecraftclicker

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.minecraftclicker.data.local.AppDatabase
import com.example.minecraftclicker.data.repository.JogoRepository
import com.example.minecraftclicker.data.repository.UpgradeRepository
import com.example.minecraftclicker.model.DEFAULT_ORES
import com.example.minecraftclicker.model.MiningBonuses
import com.example.minecraftclicker.model.UpgradeType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiningRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: JogoRepository
    private val stone = DEFAULT_ORES.first()
    private val coal = DEFAULT_ORES.first { it.id == "coal" }
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = JogoRepository(database)
    }

    @After
    fun close() = database.close()

    @Test
    fun damageIsSavedAndCoinsArriveOnlyWhenBlockBreaks() = runBlocking {
        repository.iniciar(1, stone).getOrThrow()
        repeat(4) {
            val hit = repository.minerar(1, MiningBonuses()) { coal }.getOrThrow()
            assertNull(hit.drop)
            assertEquals(0L, hit.progress.dinheiro)
            assertEquals(4 - it, hit.progress.vidaAtual)
        }
        val saved = JogoRepository(database).observarProgresso(1).first().getOrThrow()!!
        assertEquals(1, saved.vidaAtual)

        val broken = repository.minerar(1, MiningBonuses()) { coal }.getOrThrow()
        assertEquals(1L, broken.progress.dinheiro)
        assertEquals(1L, broken.progress.dinheiroTotalGanho)
        assertEquals(1L, broken.progress.blocosQuebrados)
        assertEquals(5L, broken.progress.cliques)
        assertEquals(coal.id, broken.progress.minerioId)
        assertEquals(coal.maxHealth, broken.progress.vidaAtual)
    }

    @Test
    fun fortuneMultipliesDropAndPlayersHaveSeparateProgress() = runBlocking {
        repository.iniciar(1, coal).getOrThrow()
        repository.iniciar(2, stone).getOrThrow()
        val result = repository.minerar(1, MiningBonuses(damage = 100, quantity = 3)) { stone }.getOrThrow()
        assertEquals(9L, result.progress.dinheiro)
        assertEquals(3, result.drop!!.quantity)
        val other = database.progressoJogadorDao().buscar(2)!!
        assertEquals(0L, other.dinheiro)
        assertEquals(stone.maxHealth, other.vidaAtual)
    }

    @Test
    fun concurrentHitsDoNotDuplicateRewards() = runBlocking {
        repository.iniciar(1, stone).getOrThrow()
        (1..20).map {
            async { repository.minerar(1, MiningBonuses()) { stone }.getOrThrow() }
        }.awaitAll()
        val saved = database.progressoJogadorDao().buscar(1)!!
        assertEquals(20L, saved.cliques)
        assertEquals(4L, saved.blocosQuebrados)
        assertEquals(4L, saved.dinheiro)
        assertEquals(stone.maxHealth, saved.vidaAtual)
    }

    @Test
    fun failedNextBlockRollsBackCoinsAndDamage() = runBlocking {
        repository.iniciar(1, stone).getOrThrow()
        val result = repository.minerar(1, MiningBonuses(damage = 100)) { error("Catálogo inválido") }
        assertTrue(result.isFailure)
        val saved = database.progressoJogadorDao().buscar(1)!!
        assertEquals(stone.maxHealth, saved.vidaAtual)
        assertEquals(0L, saved.dinheiro)
        assertEquals(0L, saved.cliques)
    }

    @Test
    fun bonusesComeFromSavedUpgrades() = runBlocking {
        val upgrades = UpgradeRepository(database.upgradeDao())
        upgrades.initializeDefaultUpgrades(1).getOrThrow()
        upgrades.incrementLevel(1, UpgradeType.PICKAXE).getOrThrow()
        upgrades.incrementLevel(1, UpgradeType.FORTUNE).getOrThrow()
        val bonuses = upgrades.observeMiningBonuses(1).first().getOrThrow()
        assertEquals(3, bonuses.damage)
        assertEquals(2, bonuses.quantity)
    }

    @Test
    fun migrationPreservesExistingAccountsAndUpgrades() = runBlocking {
        val name = "mining_migration_test.db"
        context.deleteDatabase(name)
        try {
            context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
                old.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL)")
                old.execSQL("CREATE UNIQUE INDEX index_users_name ON users(name)")
                old.execSQL("CREATE TABLE upgrades (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, playerId INTEGER NOT NULL, type TEXT NOT NULL, level INTEGER NOT NULL)")
                old.execSQL("CREATE UNIQUE INDEX index_upgrades_playerId_type ON upgrades(playerId, type)")
                old.execSQL("INSERT INTO users VALUES (7, 'teste', '1234', 'PLAYER')")
                old.execSQL("INSERT INTO upgrades VALUES (1, 7, 'PICKAXE', 3)")
                old.version = 3
            }
            val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_3_4)
                .build()
            try {
                assertEquals(7, migrated.userDao().getByName("teste")!!.id)
                assertEquals(3, migrated.upgradeDao().getUpgrade(7, UpgradeType.PICKAXE)!!.level)
                JogoRepository(migrated).iniciar(7, coal).getOrThrow()
                assertEquals(coal.maxHealth, migrated.progressoJogadorDao().buscar(7)!!.vidaAtual)
            } finally {
                migrated.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test
    fun damagedBlockSurvivesClosingAndReopeningDatabase() = runBlocking {
        val name = "mining_persistence_test.db"
        context.deleteDatabase(name)
        try {
            val first = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            try {
                val game = JogoRepository(first)
                game.iniciar(1, coal).getOrThrow()
                game.minerar(1, MiningBonuses()) { stone }.getOrThrow()
            } finally {
                first.close()
            }
            val reopened = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            try {
                // Entrar novamente não substitui o bloco salvo por outro sorteio.
                JogoRepository(reopened).iniciar(1, stone).getOrThrow()
                val saved = reopened.progressoJogadorDao().buscar(1)!!
                assertEquals(coal.id, saved.minerioId)
                assertEquals(coal.maxHealth - 1, saved.vidaAtual)
                assertEquals(0L, saved.dinheiro)
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
