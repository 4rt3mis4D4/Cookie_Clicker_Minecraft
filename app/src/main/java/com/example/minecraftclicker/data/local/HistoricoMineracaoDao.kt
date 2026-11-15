package com.example.minecraftclicker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class MinerioResumo(
    val minerioId: String,
    val minerioNome: String,
    val quantidade: Long
)

@Dao
interface HistoricoMineracaoDao {

    @Insert
    suspend fun inserir(historico: HistoricoMineracao)

    @Query("""
        SELECT
            minerioId,
            minerioNome,
            SUM(quantidade) AS quantidade
        FROM historico_mineracao
        WHERE jogadorId = :jogadorId
        GROUP BY minerioId, minerioNome
        ORDER BY quantidade DESC
    """)
    fun observarResumo(jogadorId: Int): Flow<List<MinerioResumo>>
}