package com.example.minecraftclicker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minecraftclicker.model.Player
import com.example.minecraftclicker.model.UpgradeItem
import com.example.minecraftclicker.model.UpgradeType
import com.example.minecraftclicker.viewmodel.UpgradesUiState
import com.example.minecraftclicker.viewmodel.UpgradesViewModel

@Composable
fun UpgradesScreen(
    viewModel: UpgradesViewModel,
    currentPlayer: Player?,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentPlayer?.id) {
        viewModel.setCurrentPlayer(currentPlayer)
    }

    UpgradesContent(
        uiState = uiState,
        onBuyUpgrade = viewModel::comprarUpgrade,
        onAddTestCoins = { viewModel.adicionarMoedasTeste(200L) },
        modifier = modifier
    )
}

@Composable
fun UpgradesContent(
    uiState: UpgradesUiState,
    onBuyUpgrade: (UpgradeType) -> Unit = {},
    onAddTestCoins: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Título e Subtítulo
        Column {
            Text(
                text = "Upgrades",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Melhore suas ferramentas para quebrar blocos mais rápido!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Card de Saldo de Moedas
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Suas Moedas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "💰 ${uiState.coins}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                OutlinedButton(
                    onClick = onAddTestCoins,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("+200 Teste")
                }
            }
        }

        // Conteúdo da Lista
        if (uiState.loading && uiState.upgrades.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Carregando seus upgrades...")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.upgrades, key = { it.type.name }) { upgrade ->
                    UpgradeCard(
                        upgrade = upgrade,
                        playerCoins = uiState.coins,
                        onBuy = { onBuyUpgrade(upgrade.type) }
                    )
                }
            }
        }
    }
}

@Composable
fun UpgradeCard(
    upgrade: UpgradeItem,
    playerCoins: Long,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = playerCoins >= upgrade.nextLevelCost
    val isMax = upgrade.isMaxLevel

    val iconEmoji = when (upgrade.type) {
        UpgradeType.PICKAXE -> "⛏️"
        UpgradeType.LUCK -> "🍀"
        UpgradeType.FORTUNE -> "💎"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = iconEmoji, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${upgrade.type.displayName} (${upgrade.tierName})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = upgrade.type.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (isMax) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (isMax) "MAX" else "Nv. ${upgrade.level}/${upgrade.maxLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (isMax) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Dano/Bônus atual: ${upgrade.currentEffect}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!isMax) {
                    Text(
                        text = "Próximo (${upgrade.nextTierName}): ${upgrade.nextEffect}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMax) {
                    Text(
                        text = "Nível Máximo Alcançado! 🏆",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Custo: 💰 ${upgrade.nextLevelCost}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = onBuy,
                    enabled = !isMax && canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        when {
                            isMax -> "Máximo"
                            !canAfford -> "Sem Moedas"
                            else -> "Melhorar"
                        }
                    )
                }
            }
        }
    }
}
