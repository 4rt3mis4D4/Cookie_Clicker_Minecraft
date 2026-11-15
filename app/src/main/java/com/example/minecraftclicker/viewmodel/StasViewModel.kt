package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.minecraftclicker.ui.stats.StatsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StatsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())

    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    fun limparErro() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}