package com.example.minecraftclicker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minecraftclicker.model.Ore
import com.example.minecraftclicker.model.Rarity
import com.example.minecraftclicker.viewmodel.AdminUiState
import com.example.minecraftclicker.viewmodel.AdminViewModel
import com.example.minecraftclicker.viewmodel.OreFormState
import com.example.minecraftclicker.viewmodel.OreSort

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Box(modifier.fillMaxSize()) {
        AdminContent(
            uiState = uiState,
            onFilterChange = viewModel::setRarityFilter,
            onSortChange = viewModel::setSort,
            onEdit = viewModel::openEditForm,
            onDelete = viewModel::requestDelete,
            onImportDefaults = viewModel::importDefaults,
            onRetry = viewModel::observeCatalog,
            onDismissError = viewModel::clearError
        )

        FloatingActionButton(
            onClick = viewModel::openCreateForm,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo minério")
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
    }

    uiState.form?.let { form ->
        OreFormDialog(
            form = form,
            saving = uiState.saving,
            error = uiState.formError,
            onNameChange = viewModel::updateName,
            onValueChange = viewModel::updateValue,
            onChanceChange = viewModel::updateChance,
            onMaxHealthChange = viewModel::updateMaxHealth,
            onRarityChange = viewModel::updateRarity,
            onIconChange = viewModel::updateIcon,
            onSave = viewModel::saveForm,
            onDismiss = viewModel::dismissForm
        )
    }

    uiState.oreToDelete?.let { ore ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Excluir minério") },
            text = { Text("Deseja excluir \"${ore.name}\" do catálogo? Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AdminContent(
    uiState: AdminUiState,
    onFilterChange: (Rarity?) -> Unit,
    onSortChange: (OreSort) -> Unit,
    onEdit: (Ore) -> Unit,
    onDelete: (Ore) -> Unit,
    onImportDefaults: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "Catálogo de minérios",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        Text(
            "${uiState.ores.size} no catálogo (Firestore)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        uiState.errorMessage?.let { message ->
            ErrorBanner(message = message, onRetry = onRetry, onDismiss = onDismissError)
        }

        FilterBar(uiState, onFilterChange, onSortChange)

        when {
            uiState.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            // Erro e nada carregado: não oferece "Importar", que também falharia
            uiState.ores.isEmpty() && uiState.errorMessage != null -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Não foi possível carregar o catálogo.\nToque em ↻ para tentar de novo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Sem erro e catálogo realmente vazio
            uiState.ores.isEmpty() -> EmptyCatalog(
                importing = uiState.saving,
                onImportDefaults = onImportDefaults
            )

            // Há minérios, mas nenhum com a raridade filtrada
            uiState.visibleOres.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nenhum minério com essa raridade.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.visibleOres, key = { it.id }) { ore ->
                    OreCard(ore = ore, onEdit = { onEdit(ore) }, onDelete = { onDelete(ore) })
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    uiState: AdminUiState,
    onFilterChange: (Rarity?) -> Unit,
    onSortChange: (OreSort) -> Unit
) {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.rarityFilter == null,
                onClick = { onFilterChange(null) },
                label = { Text("Todas (${uiState.ores.size})") }
            )
            Rarity.entries.forEach { rarity ->
                FilterChip(
                    selected = uiState.rarityFilter == rarity,
                    onClick = { onFilterChange(rarity) },
                    label = { Text("${rarity.label()} (${uiState.countByRarity[rarity] ?: 0})") }
                )
            }
        }
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ordenar por:", style = MaterialTheme.typography.labelLarge)
            OreSort.entries.forEach { sort ->
                FilterChip(
                    selected = uiState.sort == sort,
                    onClick = { onSortChange(sort) },
                    label = { Text(sort.label) }
                )
            }
        }
    }
}

@Composable
private fun OreCard(ore: Ore, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OreIconImage(icon = OreIcon.forOre(ore), fallbackText = ore.name, size = 48.dp)

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(ore.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Valor: ${ore.value}  •  Chance: ${formatChance(ore.chance)}%  •  Vida: ${ore.maxHealth}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RarityBadge(ore.rarity)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar ${ore.name}")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Excluir ${ore.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RarityBadge(rarity: Rarity) {
    Text(
        rarity.label(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(rarity.color())
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            message,
            Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = {
            onDismiss()
            onRetry()
        }) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Tentar novamente",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun EmptyCatalog(importing: Boolean, onImportDefaults: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("O catálogo está vazio", style = MaterialTheme.typography.titleMedium)
        Text(
            "Enquanto estiver vazio, o jogo usa os minérios padrão. " +
                    "Importe-os para editá-los ou toque em + para criar um novo.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onImportDefaults, enabled = !importing) {
            Text(if (importing) "Importando..." else "Importar minérios padrão")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OreFormDialog(
    form: OreFormState,
    saving: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onChanceChange: (String) -> Unit,
    onMaxHealthChange: (String) -> Unit,
    onRarityChange: (Rarity) -> Unit,
    onIconChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (form.isEditing) "Editar minério" else "Novo minério") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text("Nome") },
                    singleLine = true,
                    isError = form.nameInvalid,
                    supportingText = if (form.nameInvalid) {
                        { Text("Informe ao menos 2 caracteres") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.value,
                    onValueChange = onValueChange,
                    label = { Text("Valor (dinheiro)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = form.valueInvalid,
                    supportingText = if (form.valueInvalid) {
                        { Text("Informe um valor maior que 0") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.chance,
                    onValueChange = onChanceChange,
                    label = { Text("Chance (%)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = form.chanceInvalid,
                    supportingText = if (form.chanceInvalid) {
                        { Text("Informe um valor entre 0 e 100") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.maxHealth,
                    onValueChange = onMaxHealthChange,
                    label = { Text("Vida do bloco") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = form.maxHealthInvalid,
                    supportingText = if (form.maxHealthInvalid) {
                        { Text("Informe um valor maior que 0") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Raridade", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Rarity.entries.forEach { rarity ->
                        FilterChip(
                            selected = form.rarity == rarity,
                            onClick = { onRarityChange(rarity) },
                            label = { Text(rarity.label()) }
                        )
                    }
                }

                Text("Ícone", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconOption(
                        selected = form.icon.isBlank(),
                        onClick = { onIconChange("") }
                    ) {
                        OreIconImage(icon = null, fallbackText = form.name, size = 44.dp)
                    }
                    OreIcon.entries.forEach { icon ->
                        IconOption(
                            selected = form.icon == icon.key,
                            onClick = { onIconChange(icon.key) }
                        ) {
                            OreIconImage(icon = icon, fallbackText = icon.label, size = 44.dp)
                        }
                    }
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !saving) {
                Text(if (saving) "Salvando..." else "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
        }
    )
}

@Composable
private fun IconOption(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        content()
    }
}

private fun formatChance(chance: Double): String =
    if (chance % 1.0 == 0.0) chance.toLong().toString() else chance.toString()