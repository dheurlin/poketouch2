package xyz.heurlin.poketouch

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import xyz.heurlin.poketouch.components.EmulatorView
import xyz.heurlin.poketouch.ui.theme.PokeTouch2Theme
import xyz.heurlin.poketouch.util.withPermission

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

//        val emu = GambatteFrontend().apply {
//            loadRom(resources.openRawResource(R.raw.pokecrystal))
//        }
//
//        thread {
//            while (true) {
//                emu.retroRun()
//            }
//        }

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

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        withPermission(Manifest.permission.READ_EXTERNAL_STORAGE, this, context) {
            println("Read permission granted")
        }
        withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this, context) {
            println("Write permission granted")
        }
        return super.onCreateView(name, context, attrs)
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    PokeTouch2Theme {
        EmulatorView()
    }
}