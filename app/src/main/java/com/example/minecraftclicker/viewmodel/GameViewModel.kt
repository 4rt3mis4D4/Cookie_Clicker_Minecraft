package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftclicker.data.local.ProgressoJogador
import com.example.minecraftclicker.data.repository.JogoRepository
import com.example.minecraftclicker.data.repository.MinerioRepository
import com.example.minecraftclicker.data.repository.MiningDrop
import com.example.minecraftclicker.data.repository.UpgradeRepository
import com.example.minecraftclicker.model.DEFAULT_ORES
import com.example.minecraftclicker.model.MiningBonuses
import com.example.minecraftclicker.model.Ore
import com.example.minecraftclicker.model.drawOre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val progresso: ProgressoJogador? = null,
    val bonus: MiningBonuses = MiningBonuses(),
    val ultimoDrop: MiningDrop? = null,
    val feed: List<MiningDrop> = emptyList(),
    val loading: Boolean = true,
    val salvando: Boolean = false,
    val erro: String? = null,
    val avisoCatalogo: String? = null,
    val catalog: List<Ore> = DEFAULT_ORES
) {
    val currentBlockKey: String
        get() {
            val oreId = progresso?.minerioId.orEmpty()
            val ore = catalog.firstOrNull { it.id == oreId } ?: return oreId
            return ore.icon.ifBlank { ore.id }
        }
}

class GameViewModel(
    private val jogoRepository: JogoRepository,
    private val upgradeRepository: UpgradeRepository,
    private val minerioRepository: MinerioRepository,
    private val jogadorId: Int
) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()
    private var minerios = DEFAULT_ORES

    init {
        viewModelScope.launch {
            minerioRepository.observarMinerios().collect { result ->
                result.onSuccess { lista ->
                    val disponiveis = lista.filter {
                        it.id.isNotBlank() && it.name.isNotBlank() &&
                            it.chance.isFinite() && it.chance > 0 &&
                            it.value >= 0 && it.maxHealth > 0
                    }
                    minerios = disponiveis.ifEmpty { DEFAULT_ORES }
                    _uiState.update { it.copy(avisoCatalogo = null, catalog = minerios) }
                }.onFailure { error ->
                    minerios = DEFAULT_ORES
                    _uiState.update { it.copy(avisoCatalogo = error.message, catalog = minerios) }
                }
            }
        }

        viewModelScope.launch {
            upgradeRepository.initializeDefaultUpgrades(jogadorId)
                .onFailure { error -> _uiState.update { it.copy(erro = error.message) } }
            upgradeRepository.observeMiningBonuses(jogadorId).collect { result ->
                result.onSuccess { bonus ->
                    _uiState.update { it.copy(bonus = bonus) }
                }.onFailure { error ->
                    _uiState.update { it.copy(erro = error.message) }
                }
            }
        }

        carregarProgresso()
    }

    fun carregarProgresso() {
        if (_uiState.value.progresso != null) return
        _uiState.update { it.copy(loading = true, erro = null) }
        viewModelScope.launch {
            val result = jogoRepository.iniciar(jogadorId, drawOre(DEFAULT_ORES, 0.0))
            if (result.isFailure) {
                _uiState.update {
                    it.copy(loading = false, erro = result.exceptionOrNull()?.message)
                }
                return@launch
            }
            jogoRepository.observarProgresso(jogadorId).collect { progresso ->
                progresso.onSuccess { salvo ->
                    _uiState.update { it.copy(progresso = salvo, loading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(loading = false, erro = error.message) }
                }
            }
        }
    }

    fun minerar() {
        val estado = _uiState.value
        if (estado.loading || estado.salvando || estado.progresso == null) return

        _uiState.update { it.copy(salvando = true, erro = null) }
        viewModelScope.launch {
            jogoRepository.minerar(jogadorId, estado.bonus) {
                drawOre(minerios, estado.bonus.luck)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        progresso = result.progress,
                        ultimoDrop = result.drop ?: it.ultimoDrop,
                        feed = if (result.drop != null) {
                            (listOf(result.drop) + it.feed).take(5)
                        } else it.feed,
                        salvando = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(salvando = false, erro = error.message) }
            }
        }
    }
}
