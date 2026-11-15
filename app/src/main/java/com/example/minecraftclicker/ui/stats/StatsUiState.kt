package com.example.minecraftclicker.ui.stats

data class MinerioStat(
    val nome: String,
    val quantidade: Long
)

data class StatsUiState(
    val totalBlocos: Long? = null,
    val dinheiroTotal: Long? = null,
    val minerioMaisQuebrado: String? = null,
    val quantidadeUpgrades: Int? = null,
    val minerios: List<MinerioStat> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null
)