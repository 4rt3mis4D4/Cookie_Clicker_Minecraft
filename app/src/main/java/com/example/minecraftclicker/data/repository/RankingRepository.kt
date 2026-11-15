
package com.example.minecraftclicker.data.repository

import com.example.minecraftclicker.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RankingEntry(
    val playerId: String = "",
    val playerName: String = "",
    val score: Long = 0L
)

class RankingRepository {

    private val ranking = MutableStateFlow(
        listOf(
            RankingEntry("1", "Steve", 15000L),
            RankingEntry("2", "Alex", 12000L),
            RankingEntry("3", "Herobrine", 9500L),
            RankingEntry("4", "Notch", 8000L),
            RankingEntry("5", "Creeper", 6500L),
            RankingEntry("6", "Enderman", 5000L),
            RankingEntry("7", "Miner", 3500L),
            RankingEntry("8", "Builder", 2000L),
            RankingEntry("9", "Explorer", 1000L),
            RankingEntry("10", "CraftMaster", 500L)
        )
    )

    fun observarRanking(): Flow<List<RankingEntry>> =
        ranking.asStateFlow()

    suspend fun atualizarPontuacao(
        player: Player,
        score: Long
    ): Result<Unit> {
        ranking.update { jogadores ->
            val jogadorExistente = jogadores.any {
                it.playerId == player.id.toString()
            }

            if (jogadorExistente) {
                jogadores.map {
                    if (it.playerId == player.id.toString()) {
                        it.copy(
                            playerName = player.name,
                            score = score
                        )
                    } else {
                        it
                    }
                }
            } else {
                jogadores + RankingEntry(
                    playerId = player.id.toString(),
                    playerName = player.name,
                    score = score
                )
            }
        }

        return Result.success(Unit)
    }
}
