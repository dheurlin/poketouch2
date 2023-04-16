package xyz.heurlin.poketouch.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import xyz.heurlin.poketouch.ControllerMode

@Composable
fun ActionSelection(actions: List<() -> Unit>, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier,
    ) {
        Button(onClick = actions[0]) {
            Text("Fight")
        }
        Button(onClick = actions[1]) {
            Text("Pokémon")
        }
        Button(onClick = actions[2]) {
            Text("Pack")
        }
        Button(onClick = actions[3]) {
            Text("Run")
        }
    }
}

@Preview
@Composable
fun PreviewActionSelection() {
    ActionSelection(actions = listOf())
}