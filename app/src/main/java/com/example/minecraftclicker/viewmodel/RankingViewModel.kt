
package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftclicker.data.repository.RankingEntry
import com.example.minecraftclicker.data.repository.RankingRepository
import com.example.minecraftclicker.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingUiState(
    val ranking: List<RankingEntry> = emptyList(),
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val currentPlayerId: Int? = null
) {
    val currentPlayerPosition: Int?
        get() = ranking.indexOfFirst {
            it.playerId == currentPlayerId?.toString()
        }.takeIf { it >= 0 }?.plus(1)
}

class RankingViewModel(
    private val repository: RankingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        observarRanking()
    }

    private fun observarRanking() {
        viewModelScope.launch {
            repository.observarRanking().collect { ranking ->
                _uiState.update {
                    it.copy(
                        ranking = ranking.sortedByDescending { jogador ->
                            jogador.score
                        },
                        loading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun setCurrentPlayer(player: Player?) {
        _uiState.update {
            it.copy(currentPlayerId = player?.id)
        }
    }

    fun atualizarPontuacao(player: Player, score: Long) {
        viewModelScope.launch {
            repository.atualizarPontuacao(player, score)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message
                                ?: "Não foi possível atualizar a pontuação"
                        )
                    }
                }
        }
    }
}
