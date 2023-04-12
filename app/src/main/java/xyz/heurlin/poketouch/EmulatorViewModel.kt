package xyz.heurlin.poketouch

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

enum class ControllerMode {
    Dpad,
    MoveSelection,
    ActionSelection,
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
    data class ButtonPress(val button: Button): ControllerAction()
    data class DpadPress(val direction: DpadDirection?): ControllerAction()
}

class EmulatorViewModel: ViewModel() {
    val controllerState = ControllerState()
    var controllerMode = mutableStateOf(ControllerMode.Dpad)

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
}