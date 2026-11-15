package com.example.minecraftclicker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.minecraftclicker.model.UpgradeType
import kotlinx.coroutines.flow.Flow

@Dao
interface UpgradeDao {

    @Query("SELECT * FROM upgrades WHERE playerId = :playerId")
    fun getUpgradesForPlayer(playerId: Int): Flow<List<UpgradeEntity>>

    @Query("SELECT * FROM upgrades WHERE playerId = :playerId AND type = :type LIMIT 1")
    suspend fun getUpgrade(playerId: Int, type: UpgradeType): UpgradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(upgrade: UpgradeEntity): Long
}
