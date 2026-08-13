package com.zeyadgasser.pagecurl.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val WINDOW_NANOS = 500_000_000L
private const val NANOS_PER_SECOND = 1_000_000_000L

/**
 * Small frame-rate readout used to judge curl smoothness on real devices where a
 * debugger/Instruments connection is unavailable (e.g. TestFlight installs).
 *
 * Counts frames actually produced by Compose. While the UI is idle Compose stops
 * producing frames, so the number is only meaningful **during** a drag or animation —
 * that is exactly the moment the 60 fps question is about.
 */
@Composable
fun FpsOverlay(modifier: Modifier = Modifier) {
    var fps by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var frames = 0
        var windowStart = 0L
        while (true) {
            withFrameNanos { now ->
                if (windowStart == 0L) windowStart = now
                frames++
                val elapsed = now - windowStart
                if (elapsed >= WINDOW_NANOS) {
                    fps = ((frames.toLong() * NANOS_PER_SECOND + elapsed / 2) / elapsed).toInt()
                    frames = 0
                    windowStart = now
                }
            }
        }
    }

    Text(
        text = "$fps fps",
        color = Color.White,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
