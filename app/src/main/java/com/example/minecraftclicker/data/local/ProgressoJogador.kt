package com.example.minecraftclicker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.minecraftclicker.model.Ore

@Entity(tableName = "progresso_jogador")
data class ProgressoJogador(
    @PrimaryKey val jogadorId: Int,
    val dinheiro: Long = 0,
    val dinheiroTotalGanho: Long = 0,
    val blocosQuebrados: Long = 0,
    val cliques: Long = 0,
    val minerioId: String,
    val minerioNome: String,
    val minerioValor: Long,
    val vidaAtual: Int,
    val vidaMaxima: Int
)

fun novoProgresso(jogadorId: Int, ore: Ore) = ProgressoJogador(
    jogadorId = jogadorId,
    minerioId = ore.id,
    minerioNome = ore.name,
    minerioValor = ore.value,
    vidaAtual = ore.maxHealth,
    vidaMaxima = ore.maxHealth
)
