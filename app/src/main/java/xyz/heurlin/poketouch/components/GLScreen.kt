package xyz.heurlin.poketouch.components

import xyz.heurlin.poketouch.emulator.libretro.IScreenView
import android.content.Context
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GLScreen(onScreenCreated: (IScreenView, Context) -> Unit, modifier: Modifier = Modifier) {
    AndroidView(factory = { cxt ->
        ScreenViewGL(cxt).apply {
            onScreenCreated(this, cxt)
        }
    }, modifier = modifier.aspectRatio(160f / 144f))
}

@Preview(showSystemUi = true)
@Composable
fun PreviewGLScreen() {
    GLScreen(onScreenCreated = { screen, _ ->
        println(screen)
    })
}
