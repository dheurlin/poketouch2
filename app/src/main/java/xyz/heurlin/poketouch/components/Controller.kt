package xyz.heurlin.poketouch.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import xyz.heurlin.poketouch.ControllerAction
import xyz.heurlin.poketouch.ControllerMode
import xyz.heurlin.poketouch.types.MovePP
import xyz.heurlin.poketouch.types.PokemonMove
import xyz.heurlin.poketouch.types.PokemonType

@Composable
fun Controller(
    mode: ControllerMode,
    onButtonPressed: (ControllerAction) -> Unit,
    onClickSave: () -> Unit,
    onClickLoad: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {

        when (mode) {
            ControllerMode.Dpad -> {
                Dpad(onButtonPressed)
            }
            ControllerMode.ActionSelection -> Text("Action selection")
            ControllerMode.MoveSelection -> {
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

        Spacer(Modifier.height(15.dp))

        SaveStateMenu(
            onClickSave = onClickSave,
            onClickLoad = onClickLoad,
            modifier = Modifier.weight(1f, false)
        )
    }
}

@Composable
fun SaveStateMenu(
    onClickSave: () -> Unit,
    onClickLoad: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier
            .fillMaxWidth()
    ) {
        FloatingActionButton(
            onClick = { expanded = !expanded },
            backgroundColor = MaterialTheme.colors.primary,
            modifier = Modifier.padding(15.dp)
        ) {
            Icon(
                Icons.Filled.List,
                contentDescription = "Save states"
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(onClick = {
                onClickLoad();
                expanded = false;
            }) {
                Text("Load state")
            }
            DropdownMenuItem(onClick = {
                onClickSave();
                expanded = false;
            }) {
                Text("Save state")
            }
        }
    }
}

class ModeProvider : PreviewParameterProvider<ControllerMode> {
    override val values: Sequence<ControllerMode> = ControllerMode.values().asSequence()
}

@Preview(showSystemUi = false)
@Composable
fun PreviewController(
    @PreviewParameter(ModeProvider::class) mode: ControllerMode
) {
    Controller(mode, { }, { }, { })
}