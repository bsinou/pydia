package org.sinou.pydia.client.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat

@Composable
fun UseCellsTheme(
    customColor: String? = null,
    content: @Composable () -> Unit
) {
    CellsTheme(customColor = customColor) {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/**
 * Root theme for the Cells application based on Material3.
 */
@Composable
fun CellsTheme(
    customColor: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        customColor != null && darkTheme ->
            customDark(Color(customColor.toColorInt()).toArgb())

        customColor != null ->
            customLight(Color(customColor.toColorInt()).toArgb())

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> customDark(CellsColor.defaultMainColor.toArgb())
        else -> customLight(CellsColor.defaultMainColor.toArgb())
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val currActivity = view.context as Activity
            currActivity.window.statusBarColor = colorScheme.surfaceVariant.toArgb()
            WindowCompat.getInsetsController(
                currActivity.window,
                view
            ).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CellsTypography,
        content = content,
    )
}
