package com.example.minecraftclicker.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.minecraftclicker.R
import com.example.minecraftclicker.viewmodel.GameViewModel

@Composable
fun ClickerScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val progress = state.progresso
    val scale = remember { Animatable(1f) }
    val mineBackground = Color(0xFF253327)
    val mineText = Color(0xFFF6F0E0)
    val gold = Color(0xFFE8BB65)
    val green = Color(0xFFA6CB83)

    LaunchedEffect(progress?.cliques) {
        if ((progress?.cliques ?: 0) > 0) {
            scale.animateTo(0.93f, tween(60))
            scale.animateTo(1f, tween(120))
        }
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.loading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }

        if (progress != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiningStat("MOEDAS", "${progress.dinheiro}", Modifier.weight(1f))
                MiningStat("BLOCOS QUEBRADOS", "${progress.blocosQuebrados}", Modifier.weight(1f))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = mineBackground, contentColor = mineText)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(progress.minerioNome, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                    Box(
                        modifier = Modifier
                            .size(184.dp)
                            .scale(scale.value)
                            .border(3.dp, Color(0xFF59644F), RoundedCornerShape(8.dp))
                            .background(Color(0xFF19241B), RoundedCornerShape(8.dp))
                            .clickable(
                                enabled = !state.salvando,
                                role = Role.Button,
                                onClickLabel = "Golpear ${progress.minerioNome}",
                                onClick = viewModel::minerar
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(oreImage(progress.minerioId)),
                            contentDescription = "Bloco de ${progress.minerioNome}",
                            modifier = Modifier.size(160.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("RESISTÊNCIA", style = MaterialTheme.typography.labelMedium)
                            Text("${progress.vidaAtual} / ${progress.vidaMaxima}", style = MaterialTheme.typography.labelLarge)
                        }
                        LinearProgressIndicator(
                            progress = {
                                (progress.vidaAtual.toFloat() / progress.vidaMaxima.coerceAtLeast(1))
                                    .coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = green,
                            trackColor = Color(0xFF47523F)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MiningBonus("DANO", "${state.bonus.damage}")
                    MiningBonus("SORTE", "+${(state.bonus.luck * 100).toInt()}%")
                    MiningBonus("FORTUNA", "${state.bonus.quantity}x")
                }
            }
        }

        state.erro?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            if (progress == null && !state.loading) {
                TextButton(onClick = viewModel::carregarProgresso) { Text("Tentar novamente") }
            }
        }
        state.avisoCatalogo?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MiningStat(title: String, value: String, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MiningBonus(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@DrawableRes
private fun oreImage(id: String): Int = when (id) {
    "coal" -> R.drawable.coal_ore
    "iron" -> R.drawable.iron_ore
    "gold" -> R.drawable.gold_ore
    "redstone" -> R.drawable.redstone_ore
    "lapis" -> R.drawable.lapis_ore
    "diamond" -> R.drawable.diamond_ore
    "emerald" -> R.drawable.emerald_ore
    else -> R.drawable.cobblestone
}
