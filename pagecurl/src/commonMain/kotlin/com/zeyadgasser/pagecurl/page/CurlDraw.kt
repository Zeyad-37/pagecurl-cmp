// Forked from oleksandrbalan/pagecurl v1.5.1 (Apache-2.0) and converted to Compose Multiplatform.
// Changes vs upstream:
//   - Removed android.graphics.Bitmap/Canvas, android.os.Build, Path.asAndroidPath(), DrawScope.nativeCanvas imports
//   - java.lang.Float.max replaced with kotlin.math.max (CMP-compatible)
//   - prepareShadow() / prepareShadowApi28() / prepareShadowImage() replaced by expect fun DrawScope.drawCurlPageShadow(...)
//     so that Android and iOS can provide platform-specific shadow implementations
package com.zeyadgasser.pagecurl.page

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.zeyadgasser.pagecurl.ExperimentalPageCurlApi
import com.zeyadgasser.pagecurl.config.PageCurlConfig
import com.zeyadgasser.pagecurl.utils.Polygon
import com.zeyadgasser.pagecurl.utils.lineLineIntersection
import com.zeyadgasser.pagecurl.utils.rotate
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

@ExperimentalPageCurlApi
internal fun Modifier.drawCurl(
    config: PageCurlConfig,
    posA: Offset,
    posB: Offset,
): Modifier = drawWithCache {
    // Fast-check if curl is in left most position (gesture is fully completed)
    // In such case do not bother and draw nothing
    if (posA == size.toRect().topLeft && posB == size.toRect().bottomLeft) {
        return@drawWithCache drawNothing()
    }

    // Fast-check if curl is in right most position (gesture is not yet started)
    // In such case do not bother and draw the full content
    if (posA == size.toRect().topRight && posB == size.toRect().bottomRight) {
        return@drawWithCache drawOnlyContent()
    }

    // Find the intersection of the curl line ([posA, posB]) and top and bottom sides, so that we may clip and mirror
    // content correctly
    val topIntersection = lineLineIntersection(
        Offset(0f, 0f), Offset(size.width, 0f),
        posA, posB
    )
    val bottomIntersection = lineLineIntersection(
        Offset(0f, size.height), Offset(size.width, size.height),
        posA, posB
    )

    // Should not really happen, but in case there is no intersection (curl line is horizontal), just draw the full
    // content instead
    if (topIntersection == null || bottomIntersection == null) {
        return@drawWithCache drawOnlyContent()
    }

    // Limit x coordinates of both intersections to be at least 0, so that page does not look like it was torn
    val topCurlOffset = Offset(max(0f, topIntersection.x), topIntersection.y)
    val bottomCurlOffset = Offset(max(0f, bottomIntersection.x), bottomIntersection.y)

    // That is the easy part, prepare a lambda to draw the content clipped by the curl line
    val drawClippedContent = prepareClippedContent(topCurlOffset, bottomCurlOffset)
    // That is the tricky part, prepare a lambda to draw the back-page with the shadow
    val drawCurl = prepareCurl(config, topCurlOffset, bottomCurlOffset)

    onDrawWithContent {
        drawClippedContent()
        drawCurl()
    }
}

/**
 * The simple method to draw the whole unmodified content.
 */
private fun CacheDrawScope.drawOnlyContent(): DrawResult =
    onDrawWithContent {
        drawContent()
    }

/**
 * The simple method to draw nothing.
 */
private fun CacheDrawScope.drawNothing(): DrawResult =
    onDrawWithContent {
        /* Empty */
    }

@ExperimentalPageCurlApi
private fun CacheDrawScope.prepareClippedContent(
    topCurlOffset: Offset,
    bottomCurlOffset: Offset,
): ContentDrawScope.() -> Unit {
    // Make a quadrilateral from the left side to the intersection points
    val path = Path()
    path.lineTo(topCurlOffset.x, topCurlOffset.y)
    path.lineTo(bottomCurlOffset.x, bottomCurlOffset.y)
    path.lineTo(0f, size.height)
    return result@{
        // Draw a content clipped by the constructed path
        clipPath(path) {
            this@result.drawContent()
        }
    }
}

@ExperimentalPageCurlApi
private fun CacheDrawScope.prepareCurl(
    config: PageCurlConfig,
    topCurlOffset: Offset,
    bottomCurlOffset: Offset,
): ContentDrawScope.() -> Unit {
    // Build a quadrilateral of the part of the page which should be mirrored as the back-page
    // In all cases polygon should have 4 points, even when back-page is only a small "corner" (with 3 points) due to
    // the shadow rendering, otherwise it will create a visual artifact when switching between 3 and 4 points polygon
    val polygon = Polygon(
        sequence {
            // Find the intersection of the curl line and right side
            // If intersection is found adds to the polygon points list
            suspend fun SequenceScope<Offset>.yieldEndSideInterception() {
                val offset = lineLineIntersection(
                    topCurlOffset, bottomCurlOffset,
                    Offset(size.width, 0f), Offset(size.width, size.height)
                ) ?: return
                yield(offset)
                yield(offset)
            }

            // In case top intersection lays in the bounds of the page curl, take 2 points from the top side, otherwise
            // take the interception with a right side
            if (topCurlOffset.x < size.width) {
                yield(topCurlOffset)
                yield(Offset(size.width, topCurlOffset.y))
            } else {
                yieldEndSideInterception()
            }

            // In case bottom intersection lays in the bounds of the page curl, take 2 points from the bottom side,
            // otherwise take the interception with a right side
            if (bottomCurlOffset.x < size.width) {
                yield(Offset(size.width, size.height))
                yield(bottomCurlOffset)
            } else {
                yieldEndSideInterception()
            }
        }.toList()
    )

    // Calculate the angle in radians between X axis and the curl line, this is used to rotate mirrored content to the
    // right position of the curled back-page
    val lineVector = topCurlOffset - bottomCurlOffset
    val angle = PI.toFloat() - atan2(lineVector.y, lineVector.x) * 2

    // Prepare the platform shadow draw ONCE per cache invalidation (mirrors upstream's
    // CacheDrawScope prepareShadow): paints/paths/bitmaps are built here, and the returned lambda
    // only issues draw calls — the per-frame draw path allocates nothing.
    val drawShadow: ContentDrawScope.() -> Unit =
        if (config.shadowAlpha == 0f || config.shadowRadius == 0.dp) {
            { /* No shadow is requested */ }
        } else {
            prepareCurlPageShadow(
                polygon = polygon,
                shadowColor = config.shadowColor,
                shadowAlpha = config.shadowAlpha,
                shadowRadius = config.shadowRadius.toPx(),
                shadowOffset = Offset(-config.shadowOffset.x.toPx(), config.shadowOffset.y.toPx())
                    .rotate(2 * PI.toFloat() - angle),
            )
        }

    return result@{
        withTransform({
            // Mirror in X axis the drawing as back-page should be mirrored
            scale(-1f, 1f, pivot = bottomCurlOffset)
            // Rotate the drawing according to the curl line
            rotateRad(angle, pivot = bottomCurlOffset)
        }) {
            // Draw shadow first via the prepared platform lambda
            this@result.drawShadow()

            // And finally draw the back-page with an overlay with alpha
            clipPath(polygon.toPath()) {
                this@result.drawContent()

                val overlayAlpha = 1f - config.backPageContentAlpha
                drawRect(config.backPageColor.copy(alpha = overlayAlpha))
            }
        }
    }
}

/**
 * Prepares the platform-specific shadow rendering for the page curl effect. Runs in
 * [CacheDrawScope] (once per geometry change); the returned lambda runs per frame and must not
 * allocate.
 *
 * On Android: `Paint.setShadowLayer` + `nativeCanvas.drawPath` on API 28+; below API 28 the shadow
 * is rasterized into a software bitmap (hardware canvases only honor `setShadowLayer` for text
 * before 28 — upstream's `prepareShadowImage` fallback, required while minSdk < 28).
 * On iOS: a Skia blur `MaskFilter` on the shadow path — the same blur model as Android, so the
 * `shadowRadius`/`shadowOffset`/`shadowAlpha` config parameters mean the same thing on both
 * platforms.
 *
 * @param polygon The curled page shape (the shadow is this polygon offset outward by
 *   [shadowRadius] and shifted by [shadowOffset]).
 * @param shadowColor The base color of the shadow (alpha is controlled by [shadowAlpha]).
 * @param shadowAlpha The opacity of the shadow (0f = invisible, 1f = fully opaque).
 * @param shadowRadius The blur radius of the shadow, in pixels.
 * @param shadowOffset The offset of the shadow relative to the polygon, already rotated to match
 *   the curl angle.
 */
@ExperimentalPageCurlApi
internal expect fun CacheDrawScope.prepareCurlPageShadow(
    polygon: Polygon,
    shadowColor: Color,
    shadowAlpha: Float,
    shadowRadius: Float,
    shadowOffset: Offset,
): ContentDrawScope.() -> Unit
