// Android actual for the CMP expect fun DrawScope.drawCurlPageShadow(...)
// Uses android.graphics.Paint.setShadowLayer via drawIntoCanvas { it.nativeCanvas.drawPath(...) }.
// Pre-API-28 Bitmap fallback dropped per T-037 RFC (modern hardware supports setShadowLayer on hardware canvases).
// See docs/rfc/T-037-RFC-CMP-PageCurl-Fork.md
package com.zeyadgasser.pagecurl.page

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

@ExperimentalPageCurlApi
internal actual fun ContentDrawScope.drawCurlPageShadow(
    polygon: Polygon,
    shadowColor: Color,
    shadowAlpha: Float,
    shadowRadius: Float,
    shadowOffset: Offset,
) {
    val argbShadow = shadowColor.copy(alpha = shadowAlpha).toArgb()
    val argbTransparent = shadowColor.copy(alpha = 0f).toArgb()

    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        frameworkPaint.color = argbTransparent
        frameworkPaint.setShadowLayer(
            shadowRadius,
            shadowOffset.x,
            shadowOffset.y,
            argbShadow,
        )
    }

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawPath(
            polygon.offset(shadowRadius).toPath().asAndroidPath(),
            paint.asFrameworkPaint(),
        )
    }
}
