package com.example.minecraftclicker.model

data class UpgradeItem(
    val type: UpgradeType,
    val level: Int,
    val maxLevel: Int,
    val tierName: String,
    val nextTierName: String?,
    val nextLevelCost: Long,
    val currentEffect: String,
    val nextEffect: String
) {
    val isMaxLevel: Boolean
        get() = level >= maxLevel
}
