package xyz.heurlin.poketouch.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.heurlin.poketouch.types.PokemonType

@Composable
fun TypeBadge(type: PokemonType, modifier: Modifier = Modifier) {
    Surface(elevation = 2.dp) {
        Text(
            text = type.name.uppercase(),
            color = Color.White,
            fontSize = 11.sp,
            modifier = modifier
                .border(width = 1.dp, color = Color.White)
                .background(type.color)
                .padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Preview
@Composable
fun PreviewTypeBadge() {
    TypeBadge(type = PokemonType.Grass)
}