package xyz.heurlin.poketouch.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.heurlin.poketouch.ControllerMode
import xyz.heurlin.poketouch.EmulatorViewModel
import xyz.heurlin.poketouch.R
import xyz.heurlin.poketouch.emulator.Emulator

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

@Composable
fun EmulatorView(
    modifier: Modifier = Modifier,
    emulatorViewModel: EmulatorViewModel = viewModel()
) {
    var emulator by remember { mutableStateOf<Emulator?>(null) }
    val controllerMode = emulatorViewModel.controllerMode

    BackHandler(enabled = true) {
       emulator?.backPressed = true
    }

    Column(modifier) {
        Screen(onScreenCreated = { screen, cxt ->
            println("Screen created!, $screen")
            emulator = Emulator(
                cxt.resources.openRawResource(R.raw.pokecrystal),
                screen,
                emulatorViewModel.controllerState,
                emulatorViewModel::updateControllerMode,
                emulatorViewModel::updateControllerState,
                cxt.findActivity()
            ).apply {
                start()
                loadState()
            }
        }, modifier = Modifier.fillMaxWidth())
        Controller(
            mode = controllerMode,
            onButtonPressed =  emulatorViewModel::updateControllerState,
            onClickLoad = { emulator?.loadState() },
            onClickSave = { emulator?.saveState() },
            stopDPadRotation = emulatorViewModel::stopControllerRotation,
            updateControllerMode = emulatorViewModel::updateControllerMode,
            setTurboSpeed = { speed -> emulator?.let{ it.turboSpeed = speed }  }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewEmulatorView() {
    EmulatorView()
}