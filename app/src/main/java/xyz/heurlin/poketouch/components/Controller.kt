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
import xyz.heurlin.poketouch.exampleData.ExampleMoves

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
            is ControllerMode.Dpad -> {
                Dpad(onButtonPressed)
            }
            is ControllerMode.ActionSelection -> ActionSelection(actions = mode.actions)
            is ControllerMode.MoveSelection -> {
                MoveSelection(moves = mode.moves)
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
                onClickLoad()
                expanded = false
            }) {
                Text("Load state")
            }
            DropdownMenuItem(onClick = {
                onClickSave()
                expanded = false
            }) {
                Text("Save state")
            }
        }
    }
}

class ModeProvider : PreviewParameterProvider<ControllerMode> {
    override val values: Sequence<ControllerMode> = sequenceOf(
        ControllerMode.Dpad,
        ControllerMode.ActionSelection(actions = listOf()),
        ControllerMode.MoveSelection(ExampleMoves.moves.map {
           MoveButtonInput.Enabled(it) {}
        }),
    )
}

@Preview(showSystemUi = false)
@Composable
fun PreviewController(
    @PreviewParameter(ModeProvider::class) mode: ControllerMode
) {
    Controller(mode, { }, { }, { })
}