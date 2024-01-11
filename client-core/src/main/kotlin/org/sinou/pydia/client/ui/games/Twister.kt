package org.sinou.pydia.client.ui.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.sinou.pydia.sdk.utils.Log
import java.util.Timer
import kotlin.concurrent.schedule

private const val LOG_TAG = "Twister.kt"

@Composable
fun Twister(
) {
    val colors = listOf("red", "blue", "green", "yellow")
    val bodyParts = listOf("right foot", "left foot", "right hand", "left hand")
    LaunchedEffect(key1 = Unit) {
        Timer().schedule(0, 10000) {
            val randomColor = colors.random()
            val randomBodyPart = bodyParts.random()

            // Emit the current state
            Log.e(LOG_TAG, "Color: $randomColor, Body Part: $randomBodyPart")
        }
    }
// Timer task to emit a state every 10 seconds
}
