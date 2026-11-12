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
    val chance: Double = 0.0
)

val DEFAULT_ORES: List<Ore> = listOf(
    Ore("cobblestone", "Pedregulho", value = 1, rarity = Rarity.COMMON, chance = 50.0),
    Ore("coal", "Carvão", value = 3, rarity = Rarity.COMMON, chance = 25.0),
    Ore("iron", "Ferro", value = 8, rarity = Rarity.UNCOMMON, chance = 12.0),
    Ore("gold", "Ouro", value = 20, rarity = Rarity.RARE, chance = 6.0),
    Ore("redstone", "Redstone", value = 15, rarity = Rarity.UNCOMMON, chance = 4.0),
    Ore("lapis", "Lápis-lazúli", value = 25, rarity = Rarity.RARE, chance = 2.0),
    Ore("diamond", "Diamante", value = 100, rarity = Rarity.EPIC, chance = 0.8),
    Ore("emerald", "Esmeralda", value = 250, rarity = Rarity.LEGENDARY, chance = 0.2)
)
