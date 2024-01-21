package org.sinou.pydia.client.ui.system.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.sinou.pydia.client.R
import org.sinou.pydia.sdk.utils.Log
private const val LOG_TAG = "Splash.kt"

@Composable
fun Splash() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .padding(all = dimensionResource(R.dimen.margin))
            .wrapContentWidth(Alignment.CenterHorizontally)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Image(
                painterResource(R.drawable.logo),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentDescription = null,
                modifier = Modifier.size(240.dp)
            )
        }
        Text(
            text = stringResource(id = R.string.copyright_string),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(all = dimensionResource(R.dimen.margin))

        )
    }
}

@Composable
fun AnimatedSplashScreen(onAnimationComplete: () -> Unit) {
    // Define your splash screen UI
    // Use Compose's animation APIs to create custom animations
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.primary)
//            .padding(all = dimensionResource(R.dimen.margin))
//            .wrapContentWidth(Alignment.CenterHorizontally)
//    ) {
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f)
//        ) {
//            Image(
//                painterResource(R.drawable.logo),
//                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
//                contentDescription = null,
//                modifier = Modifier.size(240.dp)
//            )
//        }
//        Text(
//            text = stringResource(id = R.string.copyright_string),
//            color = MaterialTheme.colorScheme.onPrimary,
//            modifier = Modifier
//                .align(Alignment.End)
//                .padding(all = dimensionResource(R.dimen.margin))
//        )
//    }

    LaunchedEffect(key1 = true) {
        // Launch your animation
        Log.e(LOG_TAG, "B4 sleep")
        delay(5000L)
        Log.e(LOG_TAG, "After sleep")
        // Call onAnimationComplete when the animation is finished
        onAnimationComplete()
    }
}

