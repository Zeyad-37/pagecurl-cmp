// iOS actual for the CMP expect fun CacheDrawScope.prepareCurlPageShadow(...)
// Uses a Skia blur MaskFilter on the shadow path — the same Gaussian-blur model Android's
// setShadowLayer uses, so shadowRadius/shadowOffset/shadowAlpha render equivalently on both
// platforms. (Replaces the earlier centroid-anchored linear-gradient approximation, whose
// geometry was unrelated to the per-edge falloff the config parameters describe.)
package com.zeyadgasser.pagecurl.page

import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asSkiaPath
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.toArgb
import com.zeyadgasser.pagecurl.ExperimentalPageCurlApi
import com.zeyadgasser.pagecurl.utils.Polygon
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.Paint

// Android's BlurMaskFilter radius -> sigma conversion (see Skia's SkBlurMask::ConvertRadiusToSigma),
// used so the same shadowRadius value produces the same visual blur extent as setShadowLayer.
private const val BLUR_RADIUS_TO_SIGMA = 0.57735f
private const val BLUR_SIGMA_BIAS = 0.5f

@ExperimentalPageCurlApi
internal actual fun CacheDrawScope.prepareCurlPageShadow(
    polygon: Polygon,
    shadowColor: Color,
    shadowAlpha: Float,
    shadowRadius: Float,
    shadowOffset: Offset,
): ContentDrawScope.() -> Unit {
    // Build the blurred paint + the offset shadow path once per cache invalidation; the returned
    // per-frame lambda only issues the draw call.
    // A Skia Paint directly: only the mask filter is needed, which the Compose Paint wrapper
    // does not expose anyway. Antialias on, matching the wrapper's default.
    val frameworkPaint = Paint().apply {
        isAntiAlias = true
        color = shadowColor.copy(alpha = shadowAlpha).toArgb()
        maskFilter = MaskFilter.makeBlur(
            FilterBlurMode.NORMAL,
            shadowRadius * BLUR_RADIUS_TO_SIGMA + BLUR_SIGMA_BIAS,
        )
    }
    // setShadowLayer semantics: the blurred silhouette of the (expanded) path, drawn at the offset.
    val path = polygon
        .translate(shadowOffset)
        .offset(shadowRadius)
        .toPath()
        .asSkiaPath()

    return {
        drawIntoCanvas { canvas ->
            canvas.skiaCanvas.drawPath(path, frameworkPaint)
        }
    }
}
