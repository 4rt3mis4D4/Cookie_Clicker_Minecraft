package com.example.minecraftclicker.model

enum class Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

data class Ore(
    val id: String = "",
    val name: String = "",
    val value: Long = 0,
    val rarity: Rarity = Rarity.COMMON,
    val chance: Double = 0.0,
    val maxHealth: Int = 5
)

val DEFAULT_ORES: List<Ore> = listOf(
    Ore("cobblestone", "Pedregulho", 1, Rarity.COMMON, 50.0, maxHealth = 5),
    Ore("coal", "Carvão", 3, Rarity.COMMON, 25.0, maxHealth = 8),
    Ore("iron", "Ferro", 8, Rarity.UNCOMMON, 12.0, maxHealth = 12),
    Ore("gold", "Ouro", 20, Rarity.RARE, 6.0, maxHealth = 16),
    Ore("redstone", "Redstone", 15, Rarity.UNCOMMON, 4.0, maxHealth = 14),
    Ore("lapis", "Lápis-lazúli", 25, Rarity.RARE, 2.0, maxHealth = 18),
    Ore("diamond", "Diamante", 100, Rarity.EPIC, 0.8, maxHealth = 25),
    Ore("emerald", "Esmeralda", 250, Rarity.LEGENDARY, 0.2, maxHealth = 30)
)
