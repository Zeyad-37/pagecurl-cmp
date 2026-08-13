// Android actual for the CMP expect fun CacheDrawScope.prepareCurlPageShadow(...)
// API 28+: android.graphics.Paint.setShadowLayer via nativeCanvas.drawPath (hardware canvases
// honor setShadowLayer for arbitrary paths from API 28).
// API 26-27: hardware canvases honor setShadowLayer only for TEXT, so the shadow is rasterized
// into a software Bitmap once per geometry change and blitted per frame — upstream v1.5.1's
// prepareShadowImage fallback, reinstated because minSdk (26) predates API 28.
package com.zeyadgasser.pagecurl.page

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.zeyadgasser.pagecurl.ExperimentalPageCurlApi
import com.zeyadgasser.pagecurl.utils.Polygon

// The bitmap fallback pads the canvas by 2 * radius on each side so the blur is not clipped.
private const val BITMAP_PADDING_FACTOR = 2

@ExperimentalPageCurlApi
internal actual fun CacheDrawScope.prepareCurlPageShadow(
    polygon: Polygon,
    shadowColor: Color,
    shadowAlpha: Float,
    shadowRadius: Float,
    shadowOffset: Offset,
): ContentDrawScope.() -> Unit {
    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.color = shadowColor.copy(alpha = 0f).toArgb()
        frameworkPaint.setShadowLayer(
            shadowRadius,
            shadowOffset.x,
            shadowOffset.y,
            shadowColor.copy(alpha = shadowAlpha).toArgb(),
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val path = polygon.offset(shadowRadius).toPath().asAndroidPath()
        val frameworkPaint = paint.asFrameworkPaint()
        val draw: ContentDrawScope.() -> Unit = {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawPath(path, frameworkPaint)
            }
        }
        return draw
    } else {
        // Rasterize once per cache invalidation (per geometry change) into a padded software
        // bitmap; the per-frame draw is a single blit. Matches upstream's shipped behavior.
        val pad = BITMAP_PADDING_FACTOR * shadowRadius
        val bitmap = Bitmap.createBitmap(
            (size.width + 2 * pad).toInt().coerceAtLeast(1),
            (size.height + 2 * pad).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        Canvas(bitmap).drawPath(
            polygon
                // The bitmap is padded, so translate the polygon to keep the shadow centered.
                .translate(Offset(pad, pad))
                .offset(shadowRadius).toPath()
                .asAndroidPath(),
            paint.asFrameworkPaint(),
        )
        val draw: ContentDrawScope.() -> Unit = {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawBitmap(bitmap, -pad, -pad, null)
            }
        }
        return draw
    }
}
