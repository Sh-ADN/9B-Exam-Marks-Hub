package com.abutorab.marks9b.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.runtime.compositionLocalOf

val LocalThemeIsDark = compositionLocalOf { false }
val LocalThemeToggle = compositionLocalOf { {} }

@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = LocalThemeIsDark.current,
    onThemeToggle: () -> Unit = LocalThemeToggle.current
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Animation for the center of the "hole" that masks the circle to create the crescent moon.
    // When isDarkTheme is true, the hole moves in to cut out the moon.
    val holeX by animateFloatAsState(
        targetValue = if (isDarkTheme) 0.35f else 1.5f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "holeX"
    )
    val holeY by animateFloatAsState(
        targetValue = if (isDarkTheme) -0.35f else -1.5f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "holeY"
    )
    
    // Animation for the sun rays' length
    val raysScale by animateFloatAsState(
        targetValue = if (isDarkTheme) 0f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "rays"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isDarkTheme) -45f else 90f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "rotation"
    )

    val color by animateColorAsState(
        targetValue = if (isDarkTheme) Color(0xFFA3C9FF) else Color(0xFFFFC107), // Ocean blue / Amber
        animationSpec = tween(500),
        label = "color"
    )

    IconButton(
        onClick = onThemeToggle,
        modifier = modifier,
        interactionSource = interactionSource
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width * 0.28f
            
            rotate(rotation, center) {
                // Draw Sun Rays
                if (raysScale > 0.01f) {
                    val rayLength = size.width * 0.12f * raysScale
                    val rayOffset = size.width * 0.38f
                    for (i in 0 until 8) {
                        val angle = (i * 45) * (Math.PI / 180)
                        val start = Offset(
                            x = center.x + (cos(angle) * rayOffset).toFloat(),
                            y = center.y + (sin(angle) * rayOffset).toFloat()
                        )
                        val end = Offset(
                            x = center.x + (cos(angle) * (rayOffset + rayLength)).toFloat(),
                            y = center.y + (sin(angle) * (rayOffset + rayLength)).toFloat()
                        )
                        drawLine(
                            color = color,
                            start = start,
                            end = end,
                            strokeWidth = size.width * 0.08f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Base Circle (Sun or Moon Base)
                val baseCircle = Path().apply {
                    addOval(Rect(
                        center = center,
                        radius = baseRadius + (size.width * 0.04f * raysScale)
                    ))
                }

                // The hole that masks the circle to create a crescent
                val holeCircle = Path().apply {
                    val holeRadius = baseRadius * 0.95f
                    val holeCenter = Offset(
                        center.x + holeX * baseRadius,
                        center.y + holeY * baseRadius
                    )
                    addOval(Rect(center = holeCenter, radius = holeRadius))
                }

                val finalPath = Path().apply {
                    op(baseCircle, holeCircle, PathOperation.Difference)
                }

                drawPath(
                    path = finalPath,
                    color = color
                )
            }
        }
    }
}
