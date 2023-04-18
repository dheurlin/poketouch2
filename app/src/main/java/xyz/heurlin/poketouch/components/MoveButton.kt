package xyz.heurlin.poketouch.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.heurlin.poketouch.types.MovePP
import xyz.heurlin.poketouch.types.PokemonMove
import xyz.heurlin.poketouch.types.PokemonType
import xyz.heurlin.poketouch.ui.theme.PokeTouch2Theme
import xyz.heurlin.poketouch.util.colors.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class MoveButtonInput {
    object Disabled : MoveButtonInput()
    data class Enabled(val move: PokemonMove, val onClick: () -> Unit) : MoveButtonInput()

    @OptIn(ExperimentalContracts::class)
    fun isEnabled(): Boolean {
        contract {
            returns(true) implies (this@MoveButtonInput is Enabled)
        }
        return this is Enabled
    }
}

@Composable
fun MoveButton(
    moveInput: MoveButtonInput,
    modifier: Modifier = Modifier
) {
    val color = if (moveInput.isEnabled()) {
        moveInput.move.type.color.darken(.15f)
    }
    else {
        Color(0xFF777777)
    }

    val clickModifier: Modifier.() -> Modifier = if(moveInput.isEnabled()) { {
        this.clickable(onClick = moveInput.onClick)
    } } else { {
        this
    } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .clip(RoundedCornerShape(size = 10.dp))
            .background(color)
            .clickModifier()
            .padding(15.dp)
    ) {

        Text(
            text = if (moveInput.isEnabled()) moveInput.move.name else "",
            style = MaterialTheme.typography.body1
        )

        Row {
            if (moveInput.isEnabled()) {
                TypeBadge(type = moveInput.move.type)
            }
            val ppStr = if (moveInput.isEnabled()) {
                "PP ${moveInput.move.pp.current}/${moveInput.move.pp.total}"
            } else {
                ""
            }
            Text(
                text = ppStr,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(name = "Enabled button")
@Composable
fun PreviewMoveButton() {
    PokeTouch2Theme {
        MoveButton(
            MoveButtonInput.Enabled(
                move = PokemonMove("Seismic Toss", MovePP(10, 10), PokemonType.Fighting),
                onClick = {}
            )
        )
    }
}

@Preview(name = "Disabled button")
@Composable
fun PreviewMoveButtonDisabled() {
    PokeTouch2Theme {
        MoveButton(
            MoveButtonInput.Disabled
        )
    }
}
