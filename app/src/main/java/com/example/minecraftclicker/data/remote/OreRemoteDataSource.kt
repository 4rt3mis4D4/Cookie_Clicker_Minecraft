package com.example.minecraftclicker.data.remote

import com.example.minecraftclicker.model.Ore
import com.example.minecraftclicker.model.Rarity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.inject.Provider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OreRemoteDataSource (
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
){
    private fun collection() = firestoreProvider().collection(FirestoreCollections.ORES)

    fun observeAll(): Flow<List<Ore>> = callbackFlow {
        val registration = collection().addSnapshotListener { snapshot, error ->
            if (error != null){
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null){
                trySend(snapshot.documents.mapNotNull { it.toOre() })
            }
        }
        awaitClose { registration.remove() }
    }

    fun newId(): String = collection().document().id

    fun upsert(ore: Ore): Task<Void> =
        collection().document(ore.id).set(ore.toMap())

    fun upsetAll(ores: List<Ore>): Task<Void> {
        val db = firestoreProvider()
        val batch = db.batch()
        val col = db.collection(FirestoreCollections.ORES)
        ores.forEach { batch.set(col.document(it.id), it.toMap()) }

        return batch.commit()
    }

    fun delete(id: String): Task<Void> =
        collection().document(id).delete()
}

private fun Ore.toMap(): Map<String, Any> = mapOf(
    "name" to name,
    "value" to value,
    "rarity" to rarity.name,
    "chance" to chance,
    "icon" to icon
)

private fun DocumentSnapshot.toOre(): Ore? {
    val name = getString("name") ?: return null
    val rarity = getString("rarity")
        ?.let { runCatching { Rarity.valueOf(it) }.getOrNull() }
        ?: Rarity.COMMON
    return Ore(
        id = id,
        name = name,
        value = getLong("value") ?: 0L,
        rarity = rarity,
        chance = getDouble("chance") ?: 0.0,
        icon = getString("icon").orEmpty()
    )
}