
package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.minecraftclicker.MinecraftClickerApplication
import com.example.minecraftclicker.data.repository.RankingRepository

class AppViewModelFactory(
    private val app: MinecraftClickerApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(app.userRepository) as T
            }

            modelClass.isAssignableFrom(RankingViewModel::class.java) -> {
                RankingViewModel(RankingRepository()) as T
            }

            else -> {
                throw IllegalArgumentException(
                    "Unknown ViewModel: ${modelClass.name}"
                )
            }
        }
    }
}
