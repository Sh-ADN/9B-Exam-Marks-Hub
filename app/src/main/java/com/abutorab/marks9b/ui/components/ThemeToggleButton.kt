package com.abutorab.marks9b.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isDarkTheme) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "ThemeToggleRotation"
    )

    IconButton(
        onClick = onThemeToggle,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                rotationZ = rotation
            }
        ) {
            Crossfade(
                targetState = isDarkTheme,
                animationSpec = tween(durationMillis = 500),
                label = "ThemeToggleCrossfade"
            ) { darkTheme ->
                if (darkTheme) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Switch to light theme",
                        tint = Color(0xFFFFC107) // Amber/Gold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Nightlight,
                        contentDescription = "Switch to dark theme",
                        tint = Color(0xFF2C3E50) // Dark Blue
                    )
                }
            }
        }
    }
}
