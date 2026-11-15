package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftclicker.data.local.UpgradeEntity
import com.example.minecraftclicker.data.repository.UpgradeRepository
import com.example.minecraftclicker.model.Player
import com.example.minecraftclicker.model.UpgradeItem
import com.example.minecraftclicker.model.UpgradeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpgradesUiState(
    val upgrades: List<UpgradeItem> = emptyList(),
    val coins: Long = 0L,
    val loading: Boolean = true,
    val currentPlayerId: Int? = null
)

class UpgradesViewModel(
    private val repository: UpgradeRepository
) : ViewModel() {

    companion object {
        val PICKAXE_TIERS = listOf("Madeira", "Pedra", "Metal", "Diamante", "Netherita")
        val PICKAXE_COSTS = listOf(50L, 200L, 800L, 3000L)
        val PICKAXE_EFFECTS = listOf(
            "1 de dano por clique",
            "3 de dano por clique",
            "10 de dano por clique",
            "35 de dano por clique",
            "100 de dano por clique (Máximo)"
        )

        val LUCK_TIERS = listOf("Básica", "Sorte I", "Sorte II", "Sorte III", "Sorte IV")
        val LUCK_COSTS = listOf(100L, 350L, 1000L, 2500L)
        val LUCK_EFFECTS = listOf(
            "Chance padrão de minérios",
            "+5% de chance de minérios raros",
            "+10% de chance de minérios raros",
            "+15% de chance de minérios raros",
            "+25% de chance de minérios raros (Máximo)"
        )

        val FORTUNE_TIERS = listOf("Fortuna I", "Fortuna II", "Fortuna III")
        val FORTUNE_COSTS = listOf(500L, 2000L)
        val FORTUNE_EFFECTS = listOf(
            "1x moedas ao quebrar bloco",
            "2x moedas ao quebrar bloco",
            "3x moedas ao quebrar bloco (Máximo)"
        )
    }

    private val _uiState = MutableStateFlow(UpgradesUiState())
    val uiState: StateFlow<UpgradesUiState> = _uiState.asStateFlow()

    fun setCurrentPlayer(player: Player?) {
        val playerId = player?.id ?: return
        if (_uiState.value.currentPlayerId == playerId) return

        _uiState.update { it.copy(currentPlayerId = playerId, loading = true) }

        viewModelScope.launch {
            repository.initializeDefaultUpgrades(playerId)

            repository.observeUpgrades(playerId).collect { entities ->
                val items = buildUpgradeItems(entities)
                _uiState.update {
                    it.copy(
                        upgrades = items,
                        loading = false
                    )
                }
            }
        }
    }

    fun comprarUpgrade(type: UpgradeType) {
        val currentState = _uiState.value
        val playerId = currentState.currentPlayerId ?: return
        val item = currentState.upgrades.find { it.type == type } ?: return

        if (item.isMaxLevel || currentState.coins < item.nextLevelCost) {
            return
        }

        val cost = item.nextLevelCost

        viewModelScope.launch {
            repository.incrementLevel(playerId, type)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(coins = state.coins - cost)
                    }
                }
        }
    }

    fun adicionarMoedasTeste(quantidade: Long = 200L) {
        _uiState.update { state ->
            state.copy(coins = state.coins + quantidade)
        }
    }

    private fun buildUpgradeItems(entities: List<UpgradeEntity>): List<UpgradeItem> {
        val entityMap = entities.associateBy { it.type }

        return UpgradeType.entries.map { type ->
            val entity = entityMap[type]
            val level = entity?.level ?: 1
            criarUpgradeItem(type, level)
        }
    }

    private fun criarUpgradeItem(type: UpgradeType, level: Int): UpgradeItem {
        val maxLevel = getMaxLevel(type)
        val validLevel = level.coerceIn(1, maxLevel)
        val index = validLevel - 1

        val tierName = getTierName(type, index)
        val nextTierName = if (validLevel < maxLevel) getTierName(type, index + 1) else null
        val nextLevelCost = if (validLevel < maxLevel) getCostForNextLevel(type, index) else 0L
        val currentEffect = getEffect(type, index)
        val nextEffect = if (validLevel < maxLevel) getEffect(type, index + 1) else "Nível Máximo alcançado!"

        return UpgradeItem(
            type = type,
            level = validLevel,
            maxLevel = maxLevel,
            tierName = tierName,
            nextTierName = nextTierName,
            nextLevelCost = nextLevelCost,
            currentEffect = currentEffect,
            nextEffect = nextEffect
        )
    }

    private fun getMaxLevel(type: UpgradeType): Int = when (type) {
        UpgradeType.PICKAXE -> PICKAXE_TIERS.size
        UpgradeType.LUCK -> LUCK_TIERS.size
        UpgradeType.FORTUNE -> FORTUNE_TIERS.size
    }

    private fun getTierName(type: UpgradeType, index: Int): String = when (type) {
        UpgradeType.PICKAXE -> PICKAXE_TIERS.getOrElse(index) { "Picareta" }
        UpgradeType.LUCK -> LUCK_TIERS.getOrElse(index) { "Sorte" }
        UpgradeType.FORTUNE -> FORTUNE_TIERS.getOrElse(index) { "Fortuna" }
    }

    private fun getCostForNextLevel(type: UpgradeType, index: Int): Long = when (type) {
        UpgradeType.PICKAXE -> PICKAXE_COSTS.getOrElse(index) { 0L }
        UpgradeType.LUCK -> LUCK_COSTS.getOrElse(index) { 0L }
        UpgradeType.FORTUNE -> FORTUNE_COSTS.getOrElse(index) { 0L }
    }

    private fun getEffect(type: UpgradeType, index: Int): String = when (type) {
        UpgradeType.PICKAXE -> PICKAXE_EFFECTS.getOrElse(index) { "" }
        UpgradeType.LUCK -> LUCK_EFFECTS.getOrElse(index) { "" }
        UpgradeType.FORTUNE -> FORTUNE_EFFECTS.getOrElse(index) { "" }
    }
}
