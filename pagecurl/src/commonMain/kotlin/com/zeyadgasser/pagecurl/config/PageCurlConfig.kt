// Forked from oleksandrbalan/pagecurl v1.5.1 (Apache-2.0) and converted to Compose Multiplatform.
// Changes: replaced .java class references in listSaver with string discriminators for KMP commonMain compatibility.
@file:Suppress("ComplexMethod", "LongParameterList", "LongMethod")

package com.zeyadgasser.pagecurl.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.zeyadgasser.pagecurl.ExperimentalPageCurlApi

private const val DRAG_TYPE_GESTURE = "gesture"
private const val DRAG_TYPE_START_END = "start_end"
private const val TAP_TYPE_TARGET = "target"

/**
 * Creates a PageCurlConfig with the default properties and memorizes it.
 *
 * @param backPageColor Color of the back-page. In majority of use-cases it should be set to the content background
 * color.
 * @param backPageContentAlpha The alpha which defines how content is "seen through" the back-page. From 0 (nothing
 * is visible) to 1 (everything is visible).
 * @param shadowColor The color of the shadow. In majority of use-cases it should be set to the inverted color to the
 * content background color. Should be a solid color, see [shadowAlpha] to adjust opacity.
 * @param shadowAlpha The alpha of the [shadowColor].
 * @param shadowRadius Defines how big the shadow is.
 * @param shadowOffset Defines how shadow is shifted from the page. A little shift may add more realism.
 * @param dragForwardEnabled True if forward drag interaction is enabled or not.
 * @param dragBackwardEnabled True if backward drag interaction is enabled or not.
 * @param tapForwardEnabled True if forward tap interaction is enabled or not.
 * @param tapBackwardEnabled True if backward tap interaction is enabled or not.
 * @param tapCustomEnabled True if custom tap interaction is enabled or not, see [onCustomTap].
 * @param dragInteraction The drag interaction setting.
 * @param tapInteraction The tap interaction setting.
 * @param onCustomTap The lambda to invoke to check if tap is handled by custom tap or not. Receives the density
 * scope, the PageCurl size and tap position. Returns true if tap is handled and false otherwise.
 */
@ExperimentalPageCurlApi
@Composable
public fun rememberPageCurlConfig(
    backPageColor: Color = Color.White,
    backPageContentAlpha: Float = 0.1f,
    shadowColor: Color = Color.Black,
    shadowAlpha: Float = 0.2f,
    shadowRadius: Dp = 15.dp,
    shadowOffset: DpOffset = DpOffset((-5).dp, 0.dp),
    dragForwardEnabled: Boolean = true,
    dragBackwardEnabled: Boolean = true,
    tapForwardEnabled: Boolean = true,
    tapBackwardEnabled: Boolean = true,
    tapCustomEnabled: Boolean = true,
    dragInteraction: PageCurlConfig.DragInteraction = PageCurlConfig.StartEndDragInteraction(),
    tapInteraction: PageCurlConfig.TapInteraction = PageCurlConfig.TargetTapInteraction(),
    onCustomTap: Density.(IntSize, Offset) -> Boolean = { _, _ -> false },
): PageCurlConfig =
    rememberSaveable(
        saver = listSaver(
            save = {
                fun Rect.forSave(): List<Any> =
                    listOf(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y)

                fun PageCurlConfig.DragInteraction.getRectList(): List<Rect> =
                    when (this) {
                        is PageCurlConfig.GestureDragInteraction ->
                            listOf(forward.target, backward.target)

                        is PageCurlConfig.StartEndDragInteraction ->
                            listOf(forward.start, forward.end, backward.start, backward.end)
                    }

                fun PageCurlConfig.TapInteraction.getRectList(): List<Rect> =
                    when (this) {
                        is PageCurlConfig.TargetTapInteraction ->
                            listOf(forward.target, backward.target)
                    }

                // Use string discriminators instead of .java class references (KMP commonMain)
                fun PageCurlConfig.DragInteraction.typeKey(): String =
                    when (this) {
                        is PageCurlConfig.GestureDragInteraction -> DRAG_TYPE_GESTURE
                        is PageCurlConfig.StartEndDragInteraction -> DRAG_TYPE_START_END
                    }

                fun PageCurlConfig.DragInteraction.forSave(): List<Any> =
                    listOf(typeKey(), pointerBehavior.name) + getRectList().flatMap(Rect::forSave)

                fun PageCurlConfig.TapInteraction.forSave(): List<Any> =
                    listOf(TAP_TYPE_TARGET) + getRectList().flatMap(Rect::forSave)

                listOf(
                    (it.backPageColor.value shr 32).toInt(),
                    it.backPageContentAlpha,
                    (it.shadowColor.value shr 32).toInt(),
                    it.shadowAlpha,
                    it.shadowRadius.value,
                    it.shadowOffset.x.value,
                    it.shadowOffset.y.value,
                    it.dragForwardEnabled,
                    it.dragBackwardEnabled,
                    it.tapForwardEnabled,
                    it.tapBackwardEnabled,
                    it.tapCustomEnabled,
                    *it.dragInteraction.forSave().toTypedArray(),
                    *it.tapInteraction.forSave().toTypedArray(),
                )
            },
            restore = {
                val iterator = it.iterator()
                fun Iterator<Any>.nextRect(): Rect =
                    Rect(next() as Float, next() as Float, next() as Float, next() as Float)

                PageCurlConfig(
                    Color(iterator.next() as Int),
                    iterator.next() as Float,
                    Color(iterator.next() as Int),
                    iterator.next() as Float,
                    Dp(iterator.next() as Float),
                    DpOffset(Dp(iterator.next() as Float), Dp(iterator.next() as Float)),
                    iterator.next() as Boolean,
                    iterator.next() as Boolean,
                    iterator.next() as Boolean,
                    iterator.next() as Boolean,
                    iterator.next() as Boolean,
                    when (iterator.next() as String) {
                        DRAG_TYPE_GESTURE -> {
                            PageCurlConfig.GestureDragInteraction(
                                PageCurlConfig.DragInteraction.PointerBehavior.valueOf(iterator.next() as String),
                                PageCurlConfig.GestureDragInteraction.Config(iterator.nextRect()),
                                PageCurlConfig.GestureDragInteraction.Config(iterator.nextRect()),
                            )
                        }

                        DRAG_TYPE_START_END -> {
                            PageCurlConfig.StartEndDragInteraction(
                                PageCurlConfig.DragInteraction.PointerBehavior.valueOf(iterator.next() as String),
                                PageCurlConfig.StartEndDragInteraction.Config(iterator.nextRect(), iterator.nextRect()),
                                PageCurlConfig.StartEndDragInteraction.Config(iterator.nextRect(), iterator.nextRect()),
                            )
                        }

                        else -> error("Unable to restore PageCurlConfig DragInteraction")
                    },
                    when (iterator.next() as String) {
                        TAP_TYPE_TARGET -> {
                            PageCurlConfig.TargetTapInteraction(
                                PageCurlConfig.TargetTapInteraction.Config(iterator.nextRect()),
                                PageCurlConfig.TargetTapInteraction.Config(iterator.nextRect()),
                            )
                        }

                        else -> error("Unable to restore PageCurlConfig TapInteraction")
                    },
                    onCustomTap
                )
            }
        )
    ) {
        PageCurlConfig(
            backPageColor = backPageColor,
            backPageContentAlpha = backPageContentAlpha,
            shadowColor = shadowColor,
            shadowAlpha = shadowAlpha,
            shadowRadius = shadowRadius,
            shadowOffset = shadowOffset,
            dragForwardEnabled = dragForwardEnabled,
            dragBackwardEnabled = dragBackwardEnabled,
            tapForwardEnabled = tapForwardEnabled,
            tapBackwardEnabled = tapBackwardEnabled,
            tapCustomEnabled = tapCustomEnabled,
            dragInteraction = dragInteraction,
            tapInteraction = tapInteraction,
            onCustomTap = onCustomTap
        )
    }.also { config ->
        // Changed vs upstream: the saver restores the config entirely from SAVED values, and the
        // keyless rememberSaveable only reads the factory parameters on first creation — so a
        // theme-derived color passed here (the dominant use-case for backPageColor/shadowColor)
        // stayed pinned to the value captured at creation/save time across theme switches and
        // state restoration. Re-assert the color parameters on every call so callers passing
        // reactive theme colors always render with the current theme; the interaction fields keep
        // upstream's semantics (mutable at runtime via the config object, survive save/restore).
        config.backPageColor = backPageColor
        config.shadowColor = shadowColor
    }

/**
 * The configuration for PageCurl.
 *
 * @param backPageColor Color of the back-page. In majority of use-cases it should be set to the content background
 * color.
 * @param backPageContentAlpha The alpha which defines how content is "seen through" the back-page. From 0 (nothing
 * is visible) to 1 (everything is visible).
 * @param shadowColor The color of the shadow. In majority of use-cases it should be set to the inverted color to the
 * content background color. Should be a solid color, see [shadowAlpha] to adjust opacity.
 * @param shadowAlpha The alpha of the [shadowColor].
 * @param shadowRadius Defines how big the shadow is.
 * @param shadowOffset Defines how shadow is shifted from the page. A little shift may add more realism.
 * @param dragForwardEnabled True if forward drag interaction is enabled or not.
 * @param dragBackwardEnabled True if backward drag interaction is enabled or not.
 * @param tapForwardEnabled True if forward tap interaction is enabled or not.
 * @param tapBackwardEnabled True if backward tap interaction is enabled or not.
 * @param tapCustomEnabled True if custom tap interaction is enabled or not, see [onCustomTap].
 * @param dragInteraction The drag interaction setting.
 * @param tapInteraction The tap interaction setting.
 * @param onCustomTap The lambda to invoke to check if tap is handled by custom tap or not. Receives the density
 * scope, the PageCurl size and tap position. Returns true if tap is handled and false otherwise.
 */
@ExperimentalPageCurlApi
public class PageCurlConfig(
    backPageColor: Color,
    backPageContentAlpha: Float,
    shadowColor: Color,
    shadowAlpha: Float,
    shadowRadius: Dp,
    shadowOffset: DpOffset,
    dragForwardEnabled: Boolean,
    dragBackwardEnabled: Boolean,
    tapForwardEnabled: Boolean,
    tapBackwardEnabled: Boolean,
    tapCustomEnabled: Boolean,
    dragInteraction: DragInteraction,
    tapInteraction: TapInteraction,
    public val onCustomTap: Density.(IntSize, Offset) -> Boolean,
) {
    /** The color of the back-page. In majority of use-cases it should be set to the content background color. */
    public var backPageColor: Color by mutableStateOf(backPageColor)

    /** The alpha which defines how content is "seen through" the back-page. */
    public var backPageContentAlpha: Float by mutableStateOf(backPageContentAlpha)

    /** The color of the shadow. */
    public var shadowColor: Color by mutableStateOf(shadowColor)

    /** The alpha of the [shadowColor]. */
    public var shadowAlpha: Float by mutableStateOf(shadowAlpha)

    /** Defines how big the shadow is. */
    public var shadowRadius: Dp by mutableStateOf(shadowRadius)

    /** Defines how shadow is shifted from the page. */
    public var shadowOffset: DpOffset by mutableStateOf(shadowOffset)

    /** True if forward drag interaction is enabled or not. */
    public var dragForwardEnabled: Boolean by mutableStateOf(dragForwardEnabled)

    /** True if backward drag interaction is enabled or not. */
    public var dragBackwardEnabled: Boolean by mutableStateOf(dragBackwardEnabled)

    /** True if forward tap interaction is enabled or not. */
    public var tapForwardEnabled: Boolean by mutableStateOf(tapForwardEnabled)

    /** True if backward tap interaction is enabled or not. */
    public var tapBackwardEnabled: Boolean by mutableStateOf(tapBackwardEnabled)

    /** True if custom tap interaction is enabled or not, see [onCustomTap]. */
    public var tapCustomEnabled: Boolean by mutableStateOf(tapCustomEnabled)

    /** The drag interaction setting. */
    public var dragInteraction: DragInteraction by mutableStateOf(dragInteraction)

    /** The tap interaction setting. */
    public var tapInteraction: TapInteraction by mutableStateOf(tapInteraction)

    /** The drag interaction setting. */
    public sealed interface DragInteraction {

        /** The pointer behavior during drag interaction. */
        public val pointerBehavior: PointerBehavior

        /** The enumeration of available pointer behaviors. */
        public enum class PointerBehavior {
            /** The default behavior anchors "page flip" to the user's finger. */
            Default,

            /** In the page-edge behavior the right edge of the current page is anchored to the user's finger. */
            PageEdge;
        }
    }

    /**
     * The drag interaction setting based on where user start and end drag gesture inside the PageCurl.
     *
     * @property pointerBehavior The pointer behavior during drag interaction.
     * @property forward The forward drag configuration.
     * @property backward The backward drag configuration.
     */
    public data class StartEndDragInteraction(
        override val pointerBehavior: DragInteraction.PointerBehavior = DragInteraction.PointerBehavior.Default,
        val forward: Config = Config(start = rightHalf(), end = leftHalf()),
        val backward: Config = Config(start = leftHalf(), end = rightHalf())
    ) : DragInteraction {

        /**
         * The drag interaction configuration for forward or backward drag.
         *
         * @property start Defines a rectangle where interaction should start (relative 0..1).
         * @property end Defines a rectangle where interaction should end (relative 0..1).
         */
        public data class Config(val start: Rect, val end: Rect)
    }

    /**
     * The drag interaction setting based on the direction where drag has been started.
     *
     * @property pointerBehavior The pointer behavior during drag interaction.
     * @property forward The forward drag configuration.
     * @property backward The backward drag configuration.
     */
    public data class GestureDragInteraction(
        override val pointerBehavior: DragInteraction.PointerBehavior = DragInteraction.PointerBehavior.Default,
        val forward: Config = Config(target = full()),
        val backward: Config = Config(target = full()),
    ) : DragInteraction {

        /**
         * The drag interaction configuration for forward or backward drag.
         *
         * @property target Defines a rectangle where interaction is captured (relative 0..1).
         */
        public data class Config(val target: Rect)
    }

    /** The tap interaction setting. */
    public sealed interface TapInteraction

    /**
     * The tap interaction setting based on where user taps inside the PageCurl.
     *
     * @property forward The forward tap configuration.
     * @property backward The backward tap configuration.
     */
    public data class TargetTapInteraction(
        val forward: Config = Config(target = rightHalf()),
        val backward: Config = Config(target = leftHalf())
    ) : TapInteraction {

        /**
         * The tap interaction configuration for forward or backward tap.
         *
         * @property target Defines a rectangle where interaction is captured (relative 0..1).
         */
        public data class Config(val target: Rect)
    }
}

/** The normalised midpoint of the page (0..1 coordinate space). */
private const val PAGE_MIDPOINT = 0.5f

/** The full size of the PageCurl. */
private fun full(): Rect = Rect(0.0f, 0.0f, 1.0f, 1.0f)

/** The left half of the PageCurl. */
private fun leftHalf(): Rect = Rect(0.0f, 0.0f, PAGE_MIDPOINT, 1.0f)

/** The right half of the PageCurl. */
private fun rightHalf(): Rect = Rect(PAGE_MIDPOINT, 0.0f, 1.0f, 1.0f)
