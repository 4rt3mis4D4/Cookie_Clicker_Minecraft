package com.example.minecraftclicker.data.local

import androidx.room.TypeConverter
import com.example.minecraftclicker.model.Role
import com.example.minecraftclicker.model.UpgradeType

class Converters {

    @TypeConverter
    fun roleToString(role: Role): String = role.name

    @TypeConverter
    fun stringToRole(value: String): Role = Role.valueOf(value)

    @TypeConverter
    fun upgradeTypeToString(tipo: UpgradeType): String = tipo.name

    @TypeConverter
    fun stringToUpgradeType(value: String): UpgradeType =
        UpgradeType.valueOf(value)
}
