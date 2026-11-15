package com.example.minecraftclicker.data.repository

import com.example.minecraftclicker.data.local.UpgradeDao
import com.example.minecraftclicker.data.local.UpgradeEntity
import com.example.minecraftclicker.model.UpgradeType
import com.example.minecraftclicker.model.miningBonuses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UpgradeRepository(
    private val upgradeDao: UpgradeDao
) {

    fun observeUpgrades(playerId: Int): Flow<List<UpgradeEntity>> {
        return upgradeDao.getUpgradesForPlayer(playerId)
    }

    fun observeMiningBonuses(playerId: Int) = observeUpgrades(playerId)
        .map { upgrades ->
            fun level(type: UpgradeType) = upgrades.firstOrNull { it.type == type }?.level ?: 1
            miningBonuses(level(UpgradeType.PICKAXE), level(UpgradeType.LUCK), level(UpgradeType.FORTUNE))
        }
        .withErrorHandling("Falha ao carregar bônus dos upgrades")

    suspend fun getUpgrade(playerId: Int, type: UpgradeType): UpgradeEntity? {
        return upgradeDao.getUpgrade(playerId, type)
    }

    suspend fun initializeDefaultUpgrades(playerId: Int): Result<Unit> = safeCall("Falha ao inicializar upgrades") {
        UpgradeType.entries.forEach { type ->
            val existing = upgradeDao.getUpgrade(playerId, type)
            if (existing == null) {
                upgradeDao.insert(UpgradeEntity(playerId = playerId, type = type, level = 1))
            }
        }
    }

    suspend fun incrementLevel(playerId: Int, type: UpgradeType): Result<UpgradeEntity> =
        safeCall("Falha ao atualizar nível do upgrade") {
            val current = upgradeDao.getUpgrade(playerId, type)
            val nextLevel = (current?.level ?: 1) + 1
            val updated = current?.copy(level = nextLevel)
                ?: UpgradeEntity(playerId = playerId, type = type, level = nextLevel)

            upgradeDao.insert(updated)
            updated
        }

    suspend fun saveUpgrade(upgrade: UpgradeEntity): Result<Unit> =
        safeCall("Falha ao salvar upgrade") {
            upgradeDao.insert(upgrade)
        }
}
