package xyz.heurlin.poketouch.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.heurlin.poketouch.exampleData.ExampleMoves
import xyz.heurlin.poketouch.types.MovePP
import xyz.heurlin.poketouch.types.PokemonMove
import xyz.heurlin.poketouch.types.PokemonType
import xyz.heurlin.poketouch.ui.theme.PokeTouch2Theme

@Composable
fun MoveSelection(moves: List<MoveButtonInput>) {
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
                moves[0],
                modifier = buttonModifier
            )
            MoveButton(
                moves[1],
                modifier = buttonModifier
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = rowModifier
        ) {
            MoveButton(
                moves[2],
                modifier = buttonModifier
            )
            MoveButton(
                moves[3],
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
            moves = ExampleMoves.moves.map {
                MoveButtonInput.Enabled(it, onClick = {})
            }
        )
    }
}