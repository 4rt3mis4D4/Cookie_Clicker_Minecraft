package com.example.minecraftclicker.data.repository

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val TAG = "Repository"

suspend fun <T> safeCall(message: String, block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, message, e)
        Result.failure(Exception(message, e))
    }

fun <T> Flow<T>.withErrorHandling(message: String): Flow<Result<T>> =
    map { Result.success(it) }
        .catch { e ->
            if (e is CancellationException) throw e
            Log.e(TAG, message, e)
            emit(Result.failure(Exception(message, e)))
        }
