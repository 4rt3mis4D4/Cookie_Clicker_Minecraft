
package com.example.minecraftclicker.data.repository

import com.example.minecraftclicker.model.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class RankingEntry(
    val playerId: String = "",
    val playerName: String = "",
    val score: Long = 0L
)

class RankingRepository(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {

    private val rankingCollection =
        firestore.collection("ranking")

    fun observarRanking(): Flow<List<RankingEntry>> = callbackFlow {

        val listener = rankingCollection
            .orderBy("score", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val jogadores = snapshot?.documents?.map { document ->
                    RankingEntry(
                        playerId = document.id,
                        playerName = document.getString("playerName") ?: "",
                        score = document.getLong("score") ?: 0L
                    )

                } ?: emptyList()

                trySend(jogadores)
            }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun obterIdentificadorJogador(player: Player): String {
        val uid = obterUid()

        return "${uid}_${player.id}"
    }

    suspend fun atualizarPontuacao(
        player: Player,
        score: Long
    ): Result<Unit> {

        return try {
            val uid = obterUid()
            val documentId = "${uid}_${player.id}"

            val dados = mapOf(
                "ownerUid" to uid,
                "localPlayerId" to player.id,
                "playerName" to player.name,
                "score" to score,
                "updatedAt" to System.currentTimeMillis()
            )

            rankingCollection
                .document(documentId)
                .set(dados, SetOptions.merge())
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun obterUid(): String {
        val usuarioAtual = firebaseAuth.currentUser

        return if (usuarioAtual != null) {
            usuarioAtual.uid
        } else {
            firebaseAuth.signInAnonymously()
                .await()
                .user?.uid
                ?: throw IllegalStateException(
                    "Não foi possível autenticar o jogador."
                )
        }
    }
}
