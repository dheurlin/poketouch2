package xyz.heurlin.poketouch.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.heurlin.poketouch.ui.theme.PokeTypeGrass
import xyz.heurlin.poketouch.util.colors.darken

@Composable
fun PokeButton(
    baseColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)
) {
    val clickModifier: Modifier.() -> Modifier = if (onClick == null) {{
        this
    }} else {{
        this.clickable(onClick = onClick)
    }}

    val darkColor = baseColor.darken(0.2f)

    Box(
        Modifier
            .clip(RoundedCornerShape(size = 5.dp))
            .background(brush = Brush.verticalGradient(
                0.0f to baseColor,
                1.0f to darkColor,
                startY = 0.0f,
                endY = Float.POSITIVE_INFINITY,
            ))
            .clickModifier()
            .then(modifier)
    ) {
        ProvideTextStyle(TextStyle(
            color = Color.White,
            shadow = Shadow(
                offset = Offset(1f,1f),
                color = Color.Black,
                blurRadius = .2f,
            )
        )) {
            content()
        }
    }
}

@Preview
@Composable
fun PreviewPokeButton() {
    PokeButton(
        baseColor = PokeTypeGrass,
        onClick = {},
        modifier = Modifier
            .padding(15.dp)
    ) {
        Text("BUTTON TEST")
    }
}
