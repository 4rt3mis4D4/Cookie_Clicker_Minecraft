package com.example.minecraftclicker.data.repository

import com.example.minecraftclicker.data.local.User
import com.example.minecraftclicker.data.local.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun observeUsers(): Flow<Result<List<User>>> =
        userDao.getAll().withErrorHandling("Falha ao carregar usuários")

    suspend fun findByName(name: String): Result<User?> =
        safeCall("Falha ao buscar usuário") { userDao.getByName(name) }

    suspend fun insert(user: User): Result<Long> =
        safeCall("Falha ao criar conta") { userDao.insert(user) }

    suspend fun update(user: User): Result<Unit> =
        safeCall("Falha ao atualizar usuário") { userDao.update(user) }

    suspend fun delete(user: User): Result<Unit> =
        safeCall("Falha ao excluir usuário") { userDao.delete(user) }
}
