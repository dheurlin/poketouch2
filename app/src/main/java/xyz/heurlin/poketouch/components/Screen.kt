package xyz.heurlin.poketouch.components

import android.content.Context
import android.widget.LinearLayout
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun Screen(onScreenCreated: (ScreenView, Context) -> Unit, modifier: Modifier = Modifier) {
    AndroidView(factory = { cxt ->
        ScreenView(cxt).apply {
            onScreenCreated(this, cxt)
        }
    }, modifier = modifier.aspectRatio(160f / 144f))
}

@Preview(showSystemUi = true)
@Composable
fun PreviewScreen() {
    Screen(onScreenCreated = { screen, _ ->
        println(screen)
    })
}