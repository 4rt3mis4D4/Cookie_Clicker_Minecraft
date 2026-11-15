package com.example.minecraftclicker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.minecraftclicker.data.repository.OreRepository
import com.example.minecraftclicker.data.repository.WriteStatus
import com.example.minecraftclicker.model.Ore
import com.example.minecraftclicker.model.Rarity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val NAME_MIN = 2
private const val NAME_MAX = 24

enum class OreSort(val label: String) {
    NAME("Nome"),
    VALUE("Valor")
}

data class OreFormState(
    val editingId: String? = null,
    val name: String = "",
    val value: String = "",
    val chance: String = "",
    val maxHealth: String = "5",
    val rarity: Rarity = Rarity.COMMON,
    val icon: String = "",
    val submitAttempted: Boolean = false
) {
    val isEditing: Boolean get() = editingId != null

    val parsedValue: Long? get() = value.toLongOrNull()
    val parsedChance: Double? get() = chance.replace(',', '.').toDoubleOrNull()

    val nameInvalid: Boolean
        get() = submitAttempted && name.trim().length < NAME_MIN

    val valueInvalid: Boolean
        get() {
            val v = parsedValue
            return submitAttempted && (v == null || v <= 0L)
        }

    val chanceInvalid: Boolean
        get() {
            val c = parsedChance
            return submitAttempted && (c == null || c <= 0.0 || c > 100.0)
        }

    val parsedMaxHealth: Int? get() = maxHealth.toIntOrNull()

    val maxHealthInvalid: Boolean
        get() {
            val h = parsedMaxHealth
            return submitAttempted && (h == null || h <= 0)
        }
}

data class AdminUiState(
    val ores: List<Ore> = emptyList(),
    val visibleOres: List<Ore> = emptyList(),
    val countByRarity: Map<Rarity, Int> = emptyMap(),
    val rarityFilter: Rarity? = null,
    val sort: OreSort = OreSort.NAME,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val form: OreFormState? = null,
    val formError: String? = null,
    val oreToDelete: Ore? = null,
    val errorMessage: String? = null,
    val message: String? = null
)

class AdminViewModel(private val repository: OreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeCatalog()
    }

    fun observeCatalog() {
        observeJob?.cancel()
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        observeJob = viewModelScope.launch {
            repository.observeCatalog().collect { result ->
                result
                    .onSuccess { list ->
                        _uiState.update {
                            it.copy(ores = list, loading = false, errorMessage = null)
                                .withVisibleOres()
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(loading = false, errorMessage = e.message) }
                    }
            }
        }
    }

    fun setRarityFilter(rarity: Rarity?) {
        _uiState.update { it.copy(rarityFilter = rarity).withVisibleOres() }
    }

    fun setSort(sort: OreSort) {
        _uiState.update { it.copy(sort = sort).withVisibleOres() }
    }

    private fun AdminUiState.withVisibleOres(): AdminUiState {
        val filtered = ores.filter { ore -> rarityFilter == null || ore.rarity == rarityFilter }
        val sorted = when (sort) {
            OreSort.NAME -> filtered.sortedBy { ore -> ore.name.lowercase() }
            OreSort.VALUE -> filtered.sortedBy { ore -> ore.value }
        }
        return copy(
            visibleOres = sorted,
            countByRarity = ores.groupingBy { ore -> ore.rarity }.eachCount()
        )
    }

    fun openCreateForm() {
        _uiState.update { it.copy(form = OreFormState(), formError = null) }
    }

    fun openEditForm(ore: Ore) {
        _uiState.update {
            it.copy(
                form = OreFormState(
                    editingId = ore.id,
                    name = ore.name,
                    value = ore.value.toString(),
                    chance = ore.chance.toString(),
                    maxHealth = ore.maxHealth.toString(),
                    rarity = ore.rarity,
                    icon = ore.icon
                ),
                formError = null
            )
        }
    }

    fun dismissForm() {
        if (_uiState.value.saving) return
        _uiState.update { it.copy(form = null, formError = null) }
    }

    fun updateName(name: String) = updateForm { it.copy(name = name.take(NAME_MAX)) }

    fun updateValue(value: String) =
        updateForm { it.copy(value = value.filter(Char::isDigit).take(9)) }

    fun updateChance(chance: String) =
        updateForm { it.copy(chance = chance.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(6)) }

    fun updateMaxHealth(maxHealth: String) =
        updateForm { it.copy(maxHealth = maxHealth.filter(Char::isDigit).take(4)) }

    fun updateRarity(rarity: Rarity) = updateForm { it.copy(rarity = rarity) }

    fun updateIcon(icon: String) = updateForm { it.copy(icon = icon) }

    private fun updateForm(transform: (OreFormState) -> OreFormState) {
        _uiState.update { state ->
            state.copy(form = state.form?.let(transform), formError = null)
        }
    }

    fun saveForm() {
        val state = _uiState.value
        val form = state.form ?: return
        if (state.saving) return

        val validated = form.copy(submitAttempted = true)
        _uiState.update { it.copy(form = validated) }

        val value = validated.parsedValue
        val chance = validated.parsedChance
        val maxHealth = validated.parsedMaxHealth
        if (validated.nameInvalid || validated.valueInvalid || validated.chanceInvalid ||
            validated.maxHealthInvalid || value == null || chance == null || maxHealth == null
        ) return

        val name = validated.name.trim()
        val duplicated = state.ores.any {
            it.name.equals(name, ignoreCase = true) && it.id != validated.editingId
        }
        if (duplicated) {
            _uiState.update { it.copy(formError = "Já existe um minério com esse nome") }
            return
        }

        val ore = Ore(
            id = validated.editingId.orEmpty(),
            name = name,
            value = value,
            rarity = validated.rarity,
            chance = chance,
            icon = validated.icon,
            maxHealth = maxHealth
        )

        _uiState.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            val result = if (validated.isEditing) repository.update(ore) else repository.create(ore)
            result
                .onSuccess { status ->
                    val action = if (validated.isEditing) "atualizado" else "criado"
                    _uiState.update {
                        it.copy(
                            saving = false,
                            form = null,
                            message = status.toMessage("Minério $action")
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(saving = false, formError = e.message) }
                }
        }
    }

    fun requestDelete(ore: Ore) {
        _uiState.update { it.copy(oreToDelete = ore) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(oreToDelete = null) }
    }

    fun confirmDelete() {
        val ore = _uiState.value.oreToDelete ?: return
        _uiState.update { it.copy(oreToDelete = null, saving = true) }
        viewModelScope.launch {
            repository.delete(ore.id)
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(saving = false, message = status.toMessage("Minério excluído"))
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(saving = false, errorMessage = e.message) }
                }
        }
    }

    fun importDefaults() {
        if (_uiState.value.saving) return
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            repository.importDefaults()
                .onSuccess { status ->
                    _uiState.update {
                        it.copy(saving = false, message = status.toMessage("Minérios padrão importados"))
                    }
                }
                .onFailure { e ->
                    val detail = e.cause?.message?.let { ": $it" }.orEmpty()
                    _uiState.update { it.copy(loading = false, errorMessage = "${e.message}$detail") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun WriteStatus.toMessage(success: String): String = when (this) {
        WriteStatus.SYNCED -> success
        WriteStatus.QUEUED -> "$success localmente. Será sincronizado quando houver conexão"
    }
}