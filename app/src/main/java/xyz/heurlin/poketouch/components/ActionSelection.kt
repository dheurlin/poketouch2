package xyz.heurlin.poketouch.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.heurlin.poketouch.ui.theme.PokeTypeFighting
import xyz.heurlin.poketouch.ui.theme.PokeTypeGrass
import xyz.heurlin.poketouch.ui.theme.PokeTypeGround
import xyz.heurlin.poketouch.ui.theme.PokeTypeWater

@Composable
fun ActionSelection(actions: List<() -> Unit>, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier,
    ) {
        Box(Modifier.padding(30.dp)) {
            PokeButton(
                baseColor = PokeTypeFighting,
                onClick = actions[0],
                modifier = Modifier
                    .padding(vertical = 55.dp)
                    .fillMaxWidth()
            ) {
                ContentText("FIGHT")
            }
        }

        val smallBtnModifier = Modifier
            .padding(vertical = 25.dp)
            .weight(1f)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PokeButton(
                baseColor = PokeTypeGround,
                onClick = actions[2],
                modifier = smallBtnModifier
            ) {
                ContentText("BAG")
            }
            Spacer(Modifier.width(4.dp))
            PokeButton(
                baseColor = PokeTypeWater,
                onClick = actions[3],
                modifier = smallBtnModifier
            ) {
                ContentText("RUN")
            }
            Spacer(Modifier.width(4.dp))
            PokeButton(
                baseColor = PokeTypeGrass,
                onClick = actions[1],
                modifier = smallBtnModifier
            ) {
                ContentText("POKéMON")
            }
        }
    }
}

@Composable
private fun ContentText(text: String) {
    Text(
        text = text,
        fontSize = MaterialTheme.typography.body1.fontSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showSystemUi = true)
@Composable
fun PreviewActionSelection() {
    ActionSelection(actions = listOf({}, {}, {}, {}))
}