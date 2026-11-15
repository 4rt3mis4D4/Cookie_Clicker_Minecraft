package com.example.minecraftclicker.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "historico_mineracao",
    indices = [
        Index(value = ["jogadorId"]),
        Index(value = ["minerioId"])
    ]
)
data class HistoricoMineracao(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val jogadorId: Int,

    val minerioId: String,

    val minerioNome: String,

    val quantidade: Long
)