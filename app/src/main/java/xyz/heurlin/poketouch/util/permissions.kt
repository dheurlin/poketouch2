package xyz.heurlin.poketouch.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

private fun launcher(
    activity: ComponentActivity,
    context: Context,
    block: () -> Unit
): ActivityResultLauncher<String> {
    return activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) block()
    }
}

fun withPermission(
    permission: String,
    activity: ComponentActivity,
    context: Context,
    block: () -> Unit
) {
    val launcher = launcher(activity, context, block)
    if (ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        block()
    }

    launcher.launch(permission)
}

