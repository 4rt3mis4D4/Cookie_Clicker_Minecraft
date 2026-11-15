package com.example.minecraftclicker.data.repository

import androidx.room.withTransaction
import com.example.minecraftclicker.data.local.AppDatabase
import com.example.minecraftclicker.data.local.ProgressoJogador
import com.example.minecraftclicker.data.local.novoProgresso
import com.example.minecraftclicker.model.DEFAULT_ORES
import com.example.minecraftclicker.model.MiningBonuses
import com.example.minecraftclicker.model.Ore
import com.example.minecraftclicker.data.local.HistoricoMineracao
data class MiningDrop(val name: String, val quantity: Int, val coins: Long)

data class MiningResult(
    val progress: ProgressoJogador,
    val drop: MiningDrop? = null
)

class JogoRepository(private val database: AppDatabase) {
    private val dao = database.progressoJogadorDao()
    private val historicoDao = database.historicoMineracaoDao()

    fun observarProgresso(jogadorId: Int) =
        dao.observar(jogadorId).withErrorHandling("Falha ao carregar progresso")

    suspend fun iniciar(jogadorId: Int, ore: Ore): Result<Unit> =
        safeCall("Falha ao iniciar mineração") {
            dao.inserir(novoProgresso(jogadorId, ore))
        }

    suspend fun minerar(
        jogadorId: Int,
        bonus: MiningBonuses,
        proximo: () -> Ore
    ): Result<MiningResult> = safeCall("Falha ao salvar mineração") {
        database.withTransaction {
            val atual = checkNotNull(dao.buscar(jogadorId)) {
                "Progresso não encontrado"
            }
            val vida = (atual.vidaAtual - bonus.damage).coerceAtLeast(0)
            val drop = if (vida == 0) {
                MiningDrop(atual.minerioNome, bonus.quantity, Math.multiplyExact(atual.minerioValor, bonus.quantity.toLong()))
            } else null

            val atualizado = if (drop == null) {
                atual.copy(
                    vidaAtual = vida,
                    cliques = atual.cliques + 1
                )
            } else {

                historicoDao.inserir(
                    HistoricoMineracao(
                        jogadorId = jogadorId,
                        minerioId = atual.minerioId,
                        minerioNome = atual.minerioNome,
                        quantidade = drop.quantity.toLong()
                    )
                )

                val ore = proximo()

                atual.copy(
                    dinheiro = Math.addExact(atual.dinheiro, drop.coins),
                    dinheiroTotalGanho = Math.addExact(
                        atual.dinheiroTotalGanho,
                        drop.coins
                    ),
                    blocosQuebrados = atual.blocosQuebrados + 1,
                    cliques = atual.cliques + 1,
                    minerioId = ore.id,
                    minerioNome = ore.name,
                    minerioValor = ore.value,
                    vidaAtual = ore.maxHealth,
                    vidaMaxima = ore.maxHealth
                )
            }
            dao.atualizar(atualizado)
            MiningResult(atualizado, drop)
        }
    }

    suspend fun debitarDinheiro(jogadorId: Int, valor: Long): Result<Unit> = safeCall("Falha ao debitar moedas") {
        database.withTransaction {
            val atual = dao.buscar(jogadorId) ?: run {
                val inicial = novoProgresso(jogadorId, DEFAULT_ORES.first())
                dao.inserir(inicial)
                inicial
            }
            require(atual.dinheiro >= valor) { "Saldo insuficiente" }
            dao.atualizar(atual.copy(dinheiro = atual.dinheiro - valor))
        }
    }

    suspend fun adicionarDinheiro(jogadorId: Int, valor: Long): Result<Unit> = safeCall("Falha ao adicionar moedas") {
        database.withTransaction {
            val atual = dao.buscar(jogadorId) ?: run {
                val inicial = novoProgresso(jogadorId, DEFAULT_ORES.first())
                dao.inserir(inicial)
                inicial
            }
            dao.atualizar(
                atual.copy(
                    dinheiro = Math.addExact(atual.dinheiro, valor),
                    dinheiroTotalGanho = Math.addExact(atual.dinheiroTotalGanho, valor)
                )
            )
        }
    }
}
