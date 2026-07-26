package com.abutorab.marks9b.ui.screens

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Velocity

suspend fun PointerInputScope.detectZoomPanFling(
    onGesture: (zoom: Float, pan: Offset, centroid: Offset) -> Unit,
    onFling: (velocity: Velocity) -> Unit
) {
    awaitEachGesture {
        val velocityTracker = VelocityTracker()
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (canceled) {
                break
            }
            
            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()
            val centroid = event.calculateCentroid(useCurrent = false)
            
            if (zoomChange != 1f || panChange != Offset.Zero) {
                onGesture(zoomChange, panChange, centroid)
            }
            
            event.changes.forEach { change ->
                if (change.positionChange() != Offset.Zero) {
                    change.consume()
                }
                velocityTracker.addPosition(change.uptimeMillis, change.position)
            }
        } while (event.changes.any { it.pressed })
        
        val velocity = velocityTracker.calculateVelocity()
        onFling(velocity)
    }
}
