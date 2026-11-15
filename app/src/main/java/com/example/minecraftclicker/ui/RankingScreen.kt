
package com.example.minecraftclicker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.minecraftclicker.viewmodel.RankingViewModel

@Composable
fun RankingScreen(
    viewModel: RankingViewModel,
    currentPlayer: Player?,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentPlayer?.id) {
        viewModel.setCurrentPlayer(currentPlayer)

        currentPlayer?.let { jogador ->
            viewModel.atualizarPontuacao(
                player = jogador,
                score = 1500L
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ranking Global",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Os melhores mineradores do servidor",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        when {
            uiState.loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text("Carregando ranking...")
                }
            }

            uiState.errorMessage != null && uiState.ranking.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Não foi possível carregar o ranking.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(uiState.errorMessage.orEmpty())
                }
            }

            uiState.ranking.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Ainda não existem jogadores no ranking.")
                }
            }

            else -> {
                val top10 = uiState.ranking
                    .sortedByDescending { it.score }
                    .take(10)

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(top10) { index, player ->
                        val isCurrentPlayer =
                            player.playerId == currentPlayer?.id?.toString()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentPlayer)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = player.playerName,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isCurrentPlayer) {
                                        Text(
                                            text = "Você",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }

                                Text(
                                    text = "${player.score} moedas",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (uiState.currentPlayerPosition != null) {
                    Text(
                        text = "Sua posição: ${uiState.currentPlayerPosition}º lugar",
                        modifier = Modifier.padding(top = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Você ainda não aparece entre os 50 primeiros.",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (uiState.errorMessage != null) {
                    Text(
                        text = "Conexão instável: exibindo os últimos dados disponíveis.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
