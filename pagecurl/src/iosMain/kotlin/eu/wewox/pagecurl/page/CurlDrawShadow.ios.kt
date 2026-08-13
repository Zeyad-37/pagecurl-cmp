// iOS actual for the CMP expect fun ContentDrawScope.drawCurlPageShadow(...)
// Approximates the page-curl shadow using a Compose-native linear gradient Brush drawn over the polygon path.
// No Apple/UIKit APIs are used — this is pure Compose Multiplatform.
// See docs/rfc/T-037-RFC-CMP-PageCurl-Fork.md
package eu.wewox.pagecurl.page

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import eu.wewox.pagecurl.ExperimentalPageCurlApi
import eu.wewox.pagecurl.utils.Polygon

@ExperimentalPageCurlApi
internal actual fun ContentDrawScope.drawCurlPageShadow(
    polygon: Polygon,
    shadowColor: Color,
    shadowAlpha: Float,
    shadowRadius: Float,
    shadowOffset: Offset,
) {
    // Compute the centroid of the polygon to establish the gradient direction.
    // The gradient runs from the polygon interior (semi-transparent shadow) to the exterior (transparent),
    // approximating the visual blur produced by setShadowLayer on Android.
    val vertices = polygon.vertices
    if (vertices.size < 3) return

    val centroid = Offset(
        x = vertices.sumOf { it.x.toDouble() }.toFloat() / vertices.size,
        y = vertices.sumOf { it.y.toDouble() }.toFloat() / vertices.size,
    )

    // The shadow extends outward from the polygon edge by shadowRadius.
    // The gradient start is at the centroid (opaque shadow), end is shadowRadius beyond the edge boundary.
    val gradientEnd = centroid + shadowOffset.let { delta ->
        // Normalise the shadow offset direction and scale to shadowRadius
        val dist = delta.getDistance().coerceAtLeast(1f)
        Offset(delta.x / dist * shadowRadius, delta.y / dist * shadowRadius)
    }

    val shadowBrush = Brush.linearGradient(
        colors = listOf(
            shadowColor.copy(alpha = shadowAlpha),
            shadowColor.copy(alpha = 0f),
        ),
        start = centroid,
        end = gradientEnd,
    )

    // Draw the expanded shadow polygon filled with the gradient
    val shadowPath: Path = polygon.offset(shadowRadius).toPath()
    drawPath(path = shadowPath, brush = shadowBrush)
}
