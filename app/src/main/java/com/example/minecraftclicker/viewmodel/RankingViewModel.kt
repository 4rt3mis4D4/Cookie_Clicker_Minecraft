
package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftclicker.data.repository.JogoRepository
import com.example.minecraftclicker.data.repository.RankingEntry
import com.example.minecraftclicker.data.repository.RankingRepository
import com.example.minecraftclicker.model.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingUiState(
    val ranking: List<RankingEntry> = emptyList(),
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val currentPlayerDocumentId: String? = null
) {
    val currentPlayerPosition: Int?
        get() = ranking.indexOfFirst {
            it.playerId == currentPlayerDocumentId
        }
            .takeIf { it >= 0 }
            ?.plus(1)
}

class RankingViewModel(
    private val repository: RankingRepository,
    private val jogoRepository: JogoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private var sincronizacaoJob: Job? = null

    init {
        observarRanking()
    }

    private fun observarRanking() {
        viewModelScope.launch {
            repository.observarRanking()
                .collect { ranking ->
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
        if (player == null) {
            _uiState.update {
                it.copy(currentPlayerDocumentId = null)
            }
            return
        }

        viewModelScope.launch {
            try {
                val documentId =
                    repository.obterIdentificadorJogador(player)

                _uiState.update {
                    it.copy(currentPlayerDocumentId = documentId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message
                            ?: "Não foi possível identificar o jogador."
                    )
                }
            }
        }
    }

    fun sincronizarPontuacao(player: Player) {
        sincronizacaoJob?.cancel()

        sincronizacaoJob = viewModelScope.launch {
            jogoRepository.observarProgresso(player.id)
                .collect { resultado ->

                    resultado.onSuccess { progresso ->
                        if (progresso != null) {
                            repository.atualizarPontuacao(
                                player = player,
                                score = progresso.dinheiroTotalGanho
                            ).onFailure { error ->
                                _uiState.update {
                                    it.copy(
                                        errorMessage = error.message
                                            ?: "Não foi possível atualizar a pontuação."
                                    )
                                }
                            }
                        }
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                errorMessage = error.message
                                    ?: "Não foi possível consultar o progresso."
                            )
                        }
                    }
                }
        }
    }
}
