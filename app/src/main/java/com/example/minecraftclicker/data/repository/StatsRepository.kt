package com.example.minecraftclicker.data.repository

import com.example.minecraftclicker.data.local.AppDatabase
import com.example.minecraftclicker.data.local.HistoricoMineracao
import com.example.minecraftclicker.data.local.ProgressoJogador
import com.example.minecraftclicker.data.local.UpgradeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class StatsData(
    val progresso: ProgressoJogador?,
    val historico: List<HistoricoMineracao>,
    val upgrades: List<UpgradeEntity>
)

class StatsRepository(
    private val database: AppDatabase
) {

    fun observarEstatisticas(jogadorId: Int): Flow<Result<StatsData>> {

        return combine(
            database.progressoJogadorDao().observar(jogadorId),
            database.historicoMineracaoDao().observarTodos(jogadorId),
            database.upgradeDao().getUpgradesForPlayer(jogadorId)
        ) { progresso, historico, upgrades ->

            StatsData(
                progresso = progresso,
                historico = historico,
                upgrades = upgrades
            )
        }.withErrorHandling("Falha ao carregar estatísticas")
    }
}