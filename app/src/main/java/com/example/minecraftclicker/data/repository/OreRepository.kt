package com.example.minecraftclicker.data.repository

import android.util.Log
import com.example.minecraftclicker.data.remote.OreRemoteDataSource
import com.example.minecraftclicker.data.remote.await
import com.example.minecraftclicker.model.DEFAULT_ORES
import com.example.minecraftclicker.model.Ore
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

private val WRITE_TIMEOUT = 8.seconds

private const val TAG = "OreRepository"
private const val WRITE_TIMEOUT_MS = 8_000L

enum class WriteStatus { SYNCED, QUEUED }
class OreRepository (private val remote: OreRemoteDataSource) {
    // Admin CRUD
    fun observeCatalog(): Flow<Result<List<Ore>>> =
        remote.observeAll().withErrorHandling("Falha ao carregar o catálogo de minérios")

    suspend fun create(ore: Ore): Result<WriteStatus> =
        safeCall("Falha ao criar o minério"){
            remote.upsert(ore.copy(id = remote.newId())).awaitWrite()
        }

    suspend fun update(ore: Ore): Result<WriteStatus> =
        safeCall("Falha ao atualizar o minério") {
            require(ore.id.isNotBlank()) { "Minério sem id"}
            remote.upsert(ore).awaitWrite()
        }

    suspend fun delete(id: String): Result<WriteStatus> =
        safeCall("Falha ao excluir o minério") {
            remote.delete(id).awaitWrite()
        }

    suspend fun importDefaults(): Result<WriteStatus> =
        safeCall("Falha ao importar os minérios padrão"){
            remote.upsetAll(DEFAULT_ORES).awaitWrite()
        }

    // Lista de Minérios
    fun observeGameOres(): Flow<List<Ore>> =
        remote.observeAll()
            .map { catalog ->
                catalog.filter { it.chance > 0.0 && it.value > 0L }
                    .ifEmpty { DEFAULT_ORES }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                Log.w(TAG, "Catálogo remoto indisponível, usando minérios padrão", e)
                emit(DEFAULT_ORES)
            }

    private suspend fun Task<Void>.awaitWrite(): WriteStatus =
        withTimeoutOrNull(WRITE_TIMEOUT) {
            await()
            WriteStatus.SYNCED
        } ?: WriteStatus.QUEUED
}