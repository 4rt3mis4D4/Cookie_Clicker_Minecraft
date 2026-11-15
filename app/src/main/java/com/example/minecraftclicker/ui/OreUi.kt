package com.example.minecraftclicker.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.minecraftclicker.R
import com.example.minecraftclicker.model.Ore
import com.example.minecraftclicker.model.Rarity


enum class OreIcon(val key: String, val label: String, @DrawableRes val res: Int) {
    COAL("coal", "Carvão", R.drawable.ore_coal),
    COPPER("copper", "Cobre", R.drawable.ore_copper),
    IRON("iron", "Ferro", R.drawable.ore_iron),
    GOLD("gold", "Ouro", R.drawable.ore_gold),
    REDSTONE("redstone", "Redstone", R.drawable.ore_redstone),
    LAPIS("lapis", "Lápis-lazúli", R.drawable.ore_lapis),
    DIAMOND("diamond", "Diamante", R.drawable.ore_diamond),
    EMERALD("emerald", "Esmeralda", R.drawable.ore_emerald);

    companion object {
        fun fromKey(key: String?): OreIcon? = entries.firstOrNull { it.key == key }

        fun forOre(ore: Ore): OreIcon? =
            fromKey(ore.icon.ifBlank { ore.id })
    }
}

fun Rarity.label(): String = when (this) {
    Rarity.COMMON -> "Comum"
    Rarity.UNCOMMON -> "Incomum"
    Rarity.RARE -> "Raro"
    Rarity.EPIC -> "Épico"
    Rarity.LEGENDARY -> "Lendário"
}

fun Rarity.color(): Color = when (this) {
    Rarity.COMMON -> Color(0xFF757575)
    Rarity.UNCOMMON -> Color(0xFF2E7D32)
    Rarity.RARE -> Color(0xFF1565C0)
    Rarity.EPIC -> Color(0xFF7B1FA2)
    Rarity.LEGENDARY -> Color(0xFFEF6C00)
}

@Composable
fun OreIconImage(
    icon: OreIcon?,
    fallbackText: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    if (icon != null) {
        Image(
            bitmap = ImageBitmap.imageResource(icon.res),
            contentDescription = icon.label,
            filterQuality = FilterQuality.None,
            modifier = modifier.size(size)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackText.take(1).uppercase().ifEmpty { "?" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}