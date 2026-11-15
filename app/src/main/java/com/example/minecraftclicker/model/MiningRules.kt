package com.example.minecraftclicker.model

import kotlin.random.Random

data class MiningBonuses(
    val damage: Int = 1,
    val luck: Double = 0.0,
    val quantity: Int = 1
)

fun miningBonuses(pickaxe: Int, luck: Int, fortune: Int) = MiningBonuses(
    damage = listOf(1, 3, 10, 35, 100)[pickaxe.coerceIn(1, 5) - 1],
    luck = listOf(0.0, 0.05, 0.10, 0.15, 0.25)[luck.coerceIn(1, 5) - 1],
    quantity = fortune.coerceIn(1, 3)
)

fun oreWeight(ore: Ore, luck: Double): Double =
    ore.chance * if (ore.rarity >= Rarity.RARE) 1.0 + luck else 1.0

fun drawOre(ores: List<Ore>, luck: Double, random: Random = Random.Default): Ore {
    val available = ores.filter {
        it.chance.isFinite() && it.chance > 0 && it.value >= 0 && it.maxHealth > 0
    }
    require(available.isNotEmpty()) { "Nenhum minério disponível" }

    val weights = available.map { oreWeight(it, luck) }
    val total = weights.sum()
    require(total.isFinite() && total > 0) { "Chances de minérios inválidas" }
    var number = random.nextDouble(total)

    available.forEachIndexed { index, ore ->
        number -= weights[index]
        if (number < 0) return ore
    }
    return available.last()
}
