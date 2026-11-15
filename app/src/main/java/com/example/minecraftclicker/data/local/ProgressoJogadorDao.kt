package com.example.minecraftclicker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressoJogadorDao {
    @Query("SELECT * FROM progresso_jogador WHERE jogadorId = :jogadorId")
    fun observar(jogadorId: Int): Flow<ProgressoJogador?>

    @Query("SELECT * FROM progresso_jogador WHERE jogadorId = :jogadorId")
    suspend fun buscar(jogadorId: Int): ProgressoJogador?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(progresso: ProgressoJogador)

    @Update
    suspend fun atualizar(progresso: ProgressoJogador)

    @Query("DELETE FROM progresso_jogador WHERE jogadorId = :jogadorId")
    suspend fun excluir(jogadorId: Int)
}
