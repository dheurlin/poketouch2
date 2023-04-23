package xyz.heurlin.poketouch

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import xyz.heurlin.poketouch.components.EmulatorView
import xyz.heurlin.poketouch.components.MoveSelection
import xyz.heurlin.poketouch.emulator.Charmap
import xyz.heurlin.poketouch.emulator.Offsets
import xyz.heurlin.poketouch.emulator.libretro.GambatteFrontend
import xyz.heurlin.poketouch.types.MovePP
import xyz.heurlin.poketouch.types.PokemonMove
import xyz.heurlin.poketouch.types.PokemonType
import xyz.heurlin.poketouch.ui.theme.PokeTouch2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val emu = GambatteFrontend()
        emu.loadRom(resources.openRawResource(R.raw.pokecrystal))
        val bytes = emu.readRomBytes(Offsets.RomBankNames, Offsets.MoveNames, 50);
        val strs = Charmap.bytesToString(bytes)
        println(strs)

        setContent {
            PokeTouch2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    EmulatorView()
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    PokeTouch2Theme {
        EmulatorView()
    }
}