package xyz.heurlin.poketouch.util.colors

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.core.graphics.ColorUtils

fun ComposeColor.toAndroidColor(): AndroidColor {
    return AndroidColor.valueOf(this.value.toLong())
}

fun ComposeColor.Companion.fromAndroidColor(color: AndroidColor): ComposeColor {
    return ComposeColor(red = color.red(), green = color.green(), blue = color.blue())
}

fun ComposeColor.darken(percent: Float): ComposeColor {
    val res = ColorUtils.blendARGB(
        toAndroidColor().toArgb(),
        AndroidColor.BLACK,
        percent
    )
    val androidColor = AndroidColor.valueOf(res)
    return ComposeColor.fromAndroidColor(androidColor)
}

fun ComposeColor.lighten(percent: Float): ComposeColor {
    val res = ColorUtils.blendARGB(
        toAndroidColor().toArgb(),
        AndroidColor.WHITE,
        percent
    )
    val androidColor = AndroidColor.valueOf(res)
    return ComposeColor.fromAndroidColor(androidColor)
}
