package com.example.minecraftclicker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.minecraftclicker.model.Player
import com.example.minecraftclicker.model.Role

@Entity(
    tableName = "users",
    indices = [Index(value = ["name"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val password: String,
    val role: Role = Role.PLAYER
) {
    fun toPlayer(): Player = Player(id = id, name = name, role = role)
}
