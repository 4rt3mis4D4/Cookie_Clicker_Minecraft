package com.example.minecraftclicker.data.local

import androidx.room.TypeConverter
import com.example.minecraftclicker.model.Role

class Converters {

    @TypeConverter
    fun roleToString(role: Role): String = role.name

    @TypeConverter
    fun stringToRole(value: String): Role = Role.valueOf(value)
}
