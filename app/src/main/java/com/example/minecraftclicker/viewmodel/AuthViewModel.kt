package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftclicker.data.local.User
import com.example.minecraftclicker.data.repository.UserRepository
import com.example.minecraftclicker.model.Tab
import com.example.minecraftclicker.model.Player
import com.example.minecraftclicker.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val NAME_MIN = 3
private const val NAME_MAX = 20
private const val PASSWORD_MIN = 4
private const val PASSWORD_MAX = 30

data class AuthUiState(
    val users: List<User> = emptyList(),
    val loading: Boolean = true,
    val name: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val submitAttempted: Boolean = false,
    val submitting: Boolean = false,
    val errorMessage: String? = null,
    val currentPlayer: Player? = null
) {
    val isLoggedIn: Boolean
        get() = currentPlayer != null

    val isAdmin: Boolean
        get() = currentPlayer?.role == Role.ADMIN

    val visibleTabs: List<Tab>
        get() = Tab.entries.filter { !it.adminOnly || isAdmin }

    val namesInUse: List<String>
        get() = users.map { it.name.lowercase() }

    val nameInvalid: Boolean
        get() = submitAttempted && name.trim().length < NAME_MIN

    val passwordInvalid: Boolean
        get() = submitAttempted && password.length < PASSWORD_MIN

    val passwordsMismatch: Boolean
        get() = submitAttempted && confirmPassword != password
}

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeUsers().collect { result ->
                result
                    .onSuccess { list ->
                        _uiState.update { it.copy(users = list, loading = false) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(errorMessage = e.message, loading = false) }
                    }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name.trimStart().take(NAME_MAX), errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password.take(PASSWORD_MAX), errorMessage = null) }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password.take(PASSWORD_MAX), errorMessage = null) }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(name = "", password = "", confirmPassword = "", submitAttempted = false, errorMessage = null)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun signIn() {
        _uiState.update { it.copy(submitAttempted = true, errorMessage = null) }
        val state = _uiState.value
        if (state.nameInvalid || state.passwordInvalid) return
        if (state.loading) {
            _uiState.update { it.copy(errorMessage = "Carregando contas, tente novamente") }
            return
        }

        val name = state.name.trim()
        val user = state.users.firstOrNull {
            it.name.equals(name, ignoreCase = true) && it.password == state.password
        }

        if (user == null) {
            _uiState.update { it.copy(errorMessage = "Usuário ou senha inválidos") }
        } else {
            startSession(user.toPlayer())
        }
    }

    fun signUp() {
        _uiState.update { it.copy(submitAttempted = true, errorMessage = null) }
        val state = _uiState.value
        if (state.submitting || state.nameInvalid || state.passwordInvalid || state.passwordsMismatch) return

        val name = state.name.trim()
        if (state.namesInUse.any { it == name.lowercase() }) {
            _uiState.update { it.copy(errorMessage = "Nome já em uso") }
            return
        }

        _uiState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            repository.insert(User(name = name, password = state.password, role = Role.PLAYER))
                .onSuccess { id ->
                    _uiState.update { it.copy(submitting = false) }
                    startSession(Player(id = id.toInt(), name = name, role = Role.PLAYER))
                }
                .onFailure { e ->
                    _uiState.update { it.copy(submitting = false, errorMessage = e.message) }
                }
        }
    }

    fun logout() {
        _uiState.update { it.copy(currentPlayer = null) }
        clearForm()
    }

    private fun startSession(player: Player) {
        _uiState.update {
            it.copy(
                currentPlayer = player,
                name = "",
                password = "",
                confirmPassword = "",
                submitAttempted = false,
                errorMessage = null
            )
        }
    }
}
