package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftclicker.data.repository.StatsRepository
import com.example.minecraftclicker.ui.stats.MinerioStat
import com.example.minecraftclicker.ui.stats.StatsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatsViewModel(
    private val repository: StatsRepository,
    private val jogadorId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        StatsUiState(loading = true)
    )

    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        observarEstatisticas()
    }

    private fun observarEstatisticas() {
        viewModelScope.launch {
            repository.observarEstatisticas(jogadorId).collect { resultado ->

                resultado.onSuccess { dados ->

                    val mineriosAgrupados = dados.historico
                        .groupBy { it.minerioNome }
                        .map { (nome, registros) ->
                            MinerioStat(
                                nome = nome,
                                quantidade = registros.sumOf { it.quantidade }
                            )
                        }
                        .sortedByDescending { it.quantidade }

                    val minerioMaisQuebrado = mineriosAgrupados
                        .maxByOrNull { it.quantidade }
                        ?.nome

                    val upgradesComprados = dados.upgrades
                        .sumOf { (it.level - 1).coerceAtLeast(0) }

                    _uiState.update {
                        it.copy(
                            totalBlocos = dados.progresso?.blocosQuebrados ?: 0L,
                            dinheiroTotal = dados.progresso?.dinheiroTotalGanho ?: 0L,
                            minerioMaisQuebrado = minerioMaisQuebrado,
                            quantidadeUpgrades = upgradesComprados,
                            minerios = mineriosAgrupados,
                            loading = false,
                            errorMessage = null
                        )
                    }
                }

                resultado.onFailure { erro ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = erro.message
                                ?: "Não foi possível carregar as estatísticas."
                        )
                    }
                }
            }
        }
    }

    fun limparErro() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}