package com.example.minecraftclicker

import android.app.Application
import com.example.minecraftclicker.data.local.AppDatabase
import com.example.minecraftclicker.data.repository.UserRepository

class MinecraftClickerApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val userRepository by lazy { UserRepository(database.userDao()) }

}
