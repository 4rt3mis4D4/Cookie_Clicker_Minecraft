package com.example.minecraftclicker

import android.app.Application
import com.example.minecraftclicker.data.local.AppDatabase
import com.example.minecraftclicker.data.remote.OreRemoteDataSource
import com.example.minecraftclicker.data.repository.OreRepository
import com.example.minecraftclicker.data.repository.UpgradeRepository
import com.example.minecraftclicker.data.repository.UserRepository
import com.example.minecraftclicker.data.repository.JogoRepository
import com.example.minecraftclicker.data.repository.MinerioRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.example.minecraftclicker.data.repository.StatsRepository

class MinecraftClickerApplication : Application() {

    private val database by lazy {
        AppDatabase.getDatabase(this)
    }

    val userRepository by lazy {
        UserRepository(database.userDao())
    }

    val jogoRepository by lazy { JogoRepository(database) }

    val minerioRepository by lazy {
        MinerioRepository(
            if (FirebaseApp.getApps(this).isEmpty()) null else FirebaseFirestore.getInstance()
        )
    }

    val upgradeDao by lazy {
        database.upgradeDao()
    }

    val upgradeRepository by lazy {
        UpgradeRepository(upgradeDao)
    }

    val statsRepository by lazy {
        StatsRepository(database)
    }

    val oreRepository by lazy { OreRepository(OreRemoteDataSource()) }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}
