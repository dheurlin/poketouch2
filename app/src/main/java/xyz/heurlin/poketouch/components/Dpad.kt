package xyz.heurlin.poketouch.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.heurlin.poketouch.Button
import xyz.heurlin.poketouch.ControllerAction
import xyz.heurlin.poketouch.DpadDirection
import xyz.heurlin.poketouch.ui.theme.PokeTouch2Theme
import kotlin.math.PI
import kotlin.math.atan2

val RADIUS = 300.dp
val darkColor = Color(0xFF292929)
val lightColor = Color(0xFFD8D8D8)

@Composable
fun Dpad(
    onButtonPressed: (ControllerAction) -> Unit, modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
            .width(RADIUS)
            .aspectRatio(1f)
            .clip(CircleShape)
    ) {
        BallSurface(
            size = RADIUS,
            onDirectionChange = { onButtonPressed(ControllerAction.DpadPress(it)) },
        )
        AButton(
            onClick = { onButtonPressed(ControllerAction.ButtonPress(Button.A)) },
            onRelease = { onButtonPressed(ControllerAction.ReleaseAll) },
            size = RADIUS / 4
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalStdlibApi::class)
@Composable
fun BallSurface(
    size: Dp,
    onDirectionChange: (DpadDirection?) -> Unit,
) {
    var viewSize by remember { mutableStateOf(IntSize.Zero) }
//    var touching by remember { mutableStateOf(false) }

    fun getDirection(touchPosition: Offset): DpadDirection {
        val xCentered = touchPosition.x - (viewSize.width / 2)
        val yCentered = touchPosition.y - (viewSize.height / 2)

        return when (atan2(yCentered, xCentered)) {
            in -PI / 4 ..< PI / 4 -> DpadDirection.Right
            in (-3 * PI) / 4 ..< -PI / 4 -> DpadDirection.Up
            in PI / 4 ..< (3 * PI) / 4 -> DpadDirection.Down
            else -> DpadDirection.Left
        }
    }

    Surface(
        color = Color.Transparent,
        shape = CircleShape,
        modifier = Modifier
            .height(size)
            .width(size)
            .onGloballyPositioned { coordinates ->
                viewSize = coordinates.size
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onDirectionChange(getDirection(it))
                        awaitRelease()
                        onDirectionChange(null)
                    },
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDirectionChange(getDirection(it)) },
                    onDragEnd = {
                        onDirectionChange(null)
                    },
                ) { change, _ ->
                    onDirectionChange(getDirection(change.position))
                }
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            BallShape(RADIUS)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(RADIUS / 7)
                    .background(darkColor)
            )
            CoolShape(
                shape = CircleShape, color = darkColor, size = RADIUS / 3
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AButton(
    onClick: () -> Unit,
    onRelease: () -> Unit,
    size: Dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    if (isPressed) {
        onClick()
        DisposableEffect(Unit) {
            onDispose { onRelease() }
        }
    }

    Surface(
        onClick = {},
        interactionSource = interactionSource,
        color = lightColor,
        shape = CircleShape,
        elevation = 2.dp,
        modifier = Modifier
            .height(size)
            .width(size)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "A", fontSize = 50.sp, fontWeight = FontWeight.Bold, color = darkColor
            )
        }
    }
}

@Composable
fun BallShape(size: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
            .width(size)
            .aspectRatio(1f)
            .clip(CircleShape)
            .border(width = 7.dp, color = darkColor, shape = CircleShape)
    ) {
        Box(
            Modifier
                .width(size)
                .height(150.dp)
                .background(Color.Red)
        )
        Box(
            Modifier
                .width(size)
                .height(150.dp)
                .background(lightColor)
        )

    }
}

@Composable
fun CoolShape(shape: Shape, color: Color, size: Dp) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(color)
        )
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewDpad() {
    PokeTouch2Theme {
        Surface {
            Dpad(onButtonPressed = {})
        }
    }
}