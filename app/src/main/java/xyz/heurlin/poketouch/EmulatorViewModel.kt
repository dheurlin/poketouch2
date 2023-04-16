package xyz.heurlin.poketouch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import xyz.heurlin.poketouch.components.MoveButtonInput
import xyz.heurlin.poketouch.types.PokemonMove

sealed class ControllerMode {
    object Dpad : ControllerMode()
    data class ActionSelection(val actions: List<() -> Unit>): ControllerMode()
    data class MoveSelection(val moves: List<MoveButtonInput>) :
        ControllerMode()
}

enum class DpadDirection {
    Up,
    Down,
    Left,
    Right,
}

enum class Button {
    A,
    B,
    Select,
    Start,
}

data class ControllerState(
    var direction: DpadDirection? = null,
    var buttonsPressed: MutableMap<Button, Boolean> = mutableMapOf(
        Button.A to false,
        Button.B to false,
        Button.Select to false,
        Button.Start to false,
    )
)

sealed class ControllerAction {
    object ReleaseAll : ControllerAction()
    data class ButtonPress(val button: Button) : ControllerAction()
    data class DpadPress(val direction: DpadDirection?) : ControllerAction()
}

class EmulatorViewModel : ViewModel() {
    val controllerState = ControllerState()
    var controllerMode by mutableStateOf<ControllerMode>(ControllerMode.Dpad)
        private set

    fun updateControllerState(action: ControllerAction) {
        when (action) {
            is ControllerAction.ButtonPress -> {
                controllerState.buttonsPressed[action.button] = true
            }
            is ControllerAction.DpadPress -> {
                controllerState.direction = action.direction
            }
            is ControllerAction.ReleaseAll -> {
                controllerState.direction = null
                controllerState.buttonsPressed.keys.forEach {
                    controllerState.buttonsPressed[it] = false
                }
            }
        }
    }

    fun updateControllerMode(mode: ControllerMode) {
        controllerMode = mode
    }
}