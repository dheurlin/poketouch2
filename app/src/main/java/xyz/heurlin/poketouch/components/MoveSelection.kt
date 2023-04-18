package xyz.heurlin.poketouch.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.heurlin.poketouch.exampleData.ExampleMoves
import xyz.heurlin.poketouch.ui.theme.PokeTouch2Theme

@Composable
fun MoveSelection(moves: List<MoveButtonInput>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(2.dp)
    ) {
        items(moves) { move ->
            Box(
                Modifier.padding(2.dp)
            ) {
                MoveButton(moveInput = move)
            }
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