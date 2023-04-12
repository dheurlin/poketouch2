package xyz.heurlin.poketouch.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.heurlin.poketouch.types.MovePP
import xyz.heurlin.poketouch.types.PokemonMove
import xyz.heurlin.poketouch.types.PokemonType
import xyz.heurlin.poketouch.ui.theme.PokeTouch2Theme

@Composable
fun MoveSelection(moves: List<PokemonMove>) {
    Column() {
        val rowModifier = Modifier.fillMaxWidth()
        val buttonModifier = Modifier
            .height(100.dp)
            .weight(1f)
            .padding(8.dp)
        Row(
            modifier = rowModifier
        ) {
            MoveButton(
                move = moves[0],
                onClick = { println(moves[0].name) },
                modifier = buttonModifier
            )
            MoveButton(
                move = moves[1],
                onClick = { println(moves[1].name) },
                modifier = buttonModifier
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = rowModifier
        ) {
            MoveButton(
                move = moves[2],
                onClick = { println(moves[2].name) },
                modifier = buttonModifier
            )
            MoveButton(
                move = moves[3],
                onClick = { println(moves[3].name) },
                modifier = buttonModifier
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewMoveSelection() {
    PokeTouch2Theme {
        MoveSelection(
            moves = listOf(
                PokemonMove("Tackle", MovePP(10, 10), PokemonType.Normal),
                PokemonMove("Razor Leaf", MovePP(10, 10), PokemonType.Grass),
                PokemonMove("Dragon Rage", MovePP(10, 10), PokemonType.Dragon),
                PokemonMove("Waterfall", MovePP(10, 10), PokemonType.Water),
            )
        )
    }
}