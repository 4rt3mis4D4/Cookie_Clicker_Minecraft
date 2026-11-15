package com.example.minecraftclicker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.minecraftclicker.model.UpgradeType

@Entity(
    tableName = "upgrades",
    indices = [Index(value = ["playerId", "type"], unique = true)]
)
data class UpgradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playerId: Int,
    val type: UpgradeType,
    val level: Int = 1
)
