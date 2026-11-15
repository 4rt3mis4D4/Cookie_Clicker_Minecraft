package com.example.minecraftclicker

import com.example.minecraftclicker.model.DEFAULT_ORES
import com.example.minecraftclicker.model.Ore
import com.example.minecraftclicker.model.Rarity
import com.example.minecraftclicker.model.drawOre
import com.example.minecraftclicker.model.miningBonuses
import com.example.minecraftclicker.model.oreWeight
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class MiningRulesTest {
    @Test
    fun bonusesMatchExistingUpgradeTiers() {
        assertEquals(1, miningBonuses(1, 1, 1).damage)
        assertEquals(3, miningBonuses(2, 1, 1).damage)
        assertEquals(10, miningBonuses(3, 1, 1).damage)
        assertEquals(35, miningBonuses(4, 1, 1).damage)
        val max = miningBonuses(5, 5, 3)
        assertEquals(100, max.damage)
        assertEquals(0.25, max.luck, 0.00001)
        assertEquals(3, max.quantity)
    }

    @Test
    fun luckIncreasesRareWeightsWithoutIncreasingCommonWeights() {
        val common = DEFAULT_ORES.first()
        val diamond = DEFAULT_ORES.first { it.id == "diamond" }
        assertEquals(common.chance, oreWeight(common, 0.25), 0.00001)
        assertEquals(diamond.chance * 1.25, oreWeight(diamond, 0.25), 0.00001)
    }

    @Test
    fun weightedDrawRespectsRelativeChances() {
        val common = Ore("common", "Comum", 1, Rarity.COMMON, 3.0)
        val rare = Ore("rare", "Raro", 2, Rarity.RARE, 1.0)
        val random = Random(42)
        val draws = List(20_000) { drawOre(listOf(common, rare), 0.0, random) }
        val commonRatio = draws.count { it.id == "common" } / draws.size.toDouble()
        assertTrue(commonRatio in 0.73..0.77)
    }

    @Test
    fun unavailableOresAreNeverDrawn() {
        val valid = DEFAULT_ORES.first()
        val invalid = listOf(
            valid.copy(id = "zero", chance = 0.0),
            valid.copy(id = "negative", chance = -1.0),
            valid.copy(id = "nan", chance = Double.NaN),
            valid.copy(id = "infinite", chance = Double.POSITIVE_INFINITY),
            valid.copy(id = "dead", maxHealth = 0),
            valid.copy(id = "negativeValue", value = -1)
        )
        repeat(100) {
            assertEquals(valid, drawOre(invalid + valid, 0.0))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyCatalogDoesNotInventAReward() {
        drawOre(emptyList(), 0.0)
    }
}
