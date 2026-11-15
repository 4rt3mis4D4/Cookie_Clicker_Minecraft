package com.example.minecraftclicker.data.repository

import com.example.minecraftclicker.data.remote.FirestoreCollections
import com.example.minecraftclicker.model.DEFAULT_ORES
import com.example.minecraftclicker.model.Ore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch

class MinerioRepository(private val firestore: FirebaseFirestore?) {
    fun observarMinerios(): Flow<Result<List<Ore>>> = callbackFlow {
        val database = firestore
        if (database == null) {
            trySend(Result.success(DEFAULT_ORES))
            close()
            return@callbackFlow
        }

        val listener = database.collection(FirestoreCollections.ORES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(Exception("Catálogo offline: usando minérios padrão", error)))
                } else {
                    try {
                        val ores = snapshot?.documents.orEmpty().mapNotNull { document ->
                            document.toObject(Ore::class.java)?.copy(id = document.id)
                        }
                        trySend(Result.success(ores))
                    } catch (e: Exception) {
                        trySend(Result.failure(Exception("Erro no catálogo: usando minérios padrão", e)))
                    }
                }
            }
        awaitClose { listener.remove() }
    }.catch { error ->
        if (error is CancellationException) throw error
        emit(Result.failure(Exception("Falha ao acessar catálogo: usando minérios padrão", error)))
    }
}
